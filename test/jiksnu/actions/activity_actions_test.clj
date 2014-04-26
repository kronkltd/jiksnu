(ns jiksnu.actions.activity-actions-test
  (:require [ciste.core :refer [with-context]]
            [ciste.sections.default :refer [show-section]]
            [clj-factory.core :refer [factory fseq]]
            [clojure.tools.logging :as log]
            [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.actions.domain-actions :as actions.domain]
            [jiksnu.db :as db]
            [jiksnu.mock :as mock]
            [jiksnu.model :as model]
            [jiksnu.model.activity :as model.activity]
            jiksnu.modules.atom.sections.activity-sections
            jiksnu.modules.atom.sections.user-sections
            [jiksnu.modules.atom.util :as abdera]
            [jiksnu.session :as session]
            [jiksnu.test-helper :refer [check context future-context test-environment-fixture]]
            [jiksnu.util :as util]
            [midje.sweet :refer [=> contains throws truthy falsey]])
  (:import jiksnu.model.Activity))

(test-environment-fixture

 (context #'actions.activity/oembed->activity
   (let [oembed-str (slurp "test-resources/oembed.json")]
     ;; TODO: complete
     oembed-str => string?))

 (context #'actions.activity/entry->activity
   (let [domain-name (fseq :domain)
         domain (-> (factory :domain
                             {:discovered true
                              :_id domain-name})
                    actions.domain/find-or-create
                    (actions.domain/add-link {:rel "lrdd"
                                              :template
                                              (str "http://" domain-name "/lrdd?uri={uri}")}))
         user (mock/a-user-exists {:domain domain})]

     ;; TODO: Load elements from resources
     (context "should return an Activity"
       (with-context [:http :atom]
         (let [activity (mock/there-is-an-activity {:user user})
               entry (show-section activity)]
           (actions.activity/entry->activity entry) => (partial instance? Activity))))

     (future-context "when coming from an identi.ca feed"
       (context "should parse the published field"
         (let [feed (abdera/load-file "identica-update.xml")
               entry (first (abdera/get-entries feed))]
           (actions.activity/entry->activity entry) => (partial instance? Activity))))
     ))

 (context #'actions.activity/find-by-user
   (context "when the user has activities"
     (db/drop-all!)
     (let [user (mock/a-user-exists)
           activity (mock/there-is-an-activity {:user user})]
       (actions.activity/find-by-user user) =>
       (check [response]
         response => map?
         (:totalRecords response) => 1
         (count (:items response)) => 1
         (doseq [item (:items response)]
           item => (partial instance? Activity))))))

 (context #'actions.activity/create
   (context "when the user is logged in"
     (context "and it is a valid activity"
       (context "should return that activity"
         (let [domain (mock/a-domain-exists)
               feed-source (mock/a-feed-source-exists)
               conversation (mock/a-conversation-exists)
               user (mock/a-user-exists)
               activity (factory :activity {:author        (:_id user)
                                            :conversation  (:_id conversation)
                                            :update-source (:_id feed-source)
                                            :local         true})]
           (actions.activity/create activity) => (partial instance? Activity))))))

 (context #'actions.activity/post
   (context "when the user is not logged in"
     (let [activity (dissoc (factory :activity) :author)]
       (actions.activity/post activity) => (throws RuntimeException))))

 (context #'actions.activity/delete
   (context "when the activity exists"
     (context "and the user owns the activity"
       (context "should delete that activity"
         (let [user (mock/a-user-exists)]
           (session/with-user user
             (let [activity (mock/there-is-an-activity {:user user})]
               (actions.activity/delete activity) => activity
               (model.activity/fetch-by-id (:_id activity)) => nil)))))
     (context "and the user does not own the activity"
       (context "should not delete that activity"
         (let [user (mock/a-user-exists)
               author (mock/a-remote-user-exists)
               activity (mock/there-is-an-activity {:user author})]
           (session/with-user user
             (actions.activity/delete activity) => (throws RuntimeException)
             (model.activity/fetch-by-id (:_id activity)) => activity))))))

 (context #'actions.activity/viewable?
   (context "When it is public"
     (let [activity (mock/there-is-an-activity)]
       (actions.activity/viewable? activity .user.)) => truthy)
   (context "when it is not public"
     (context "when the user is the author"
       (let [user (mock/a-user-exists)
             activity (mock/there-is-an-activity {:user user})]
         (actions.activity/viewable? activity user)) => truthy)
     (context "when the user is not the author"
       (context "when the user is an admin"
         (let [user (mock/a-user-exists {:admin true})
               activity (mock/there-is-an-activity {:modifier "private"})]
           (actions.activity/viewable? activity user)) => truthy)
       (context "when the user is not an admin"
         (let [user (mock/a-user-exists)
               author (mock/a-user-exists)
               activity (mock/there-is-an-activity {:modifier "private"
                                                    :user author})]
           (actions.activity/viewable? activity user)) => falsey))))

 (context #'actions.activity/show
   (context "when the record exists"
     (context "and the record is viewable"
       (let [activity (mock/there-is-an-activity)]
         (actions.activity/show activity) => activity
         (provided
           (actions.activity/viewable? activity) => true)))
     (context "and the record is not viewable"
       (let [activity (mock/there-is-an-activity)]
         (actions.activity/show activity) => (throws RuntimeException)
         (provided
           (actions.activity/viewable? activity) => false)))))

 (context #'actions.activity/edit

   (context "when the params are nil"
     (let [params nil]
       (actions.activity/edit params) => (throws)))

   (context "when there is not an id"
     (let [params {}]
       (actions.activity/edit params) => (throws)))

   (context "when the target is not found"
     (let [params {:_id (util/make-id)}]
       (actions.activity/edit params) => (throws)))

   (context "when there is no actor"
     (let [activity (mock/there-is-an-activity)
           params {:_id (:_id activity)}]
       (actions.activity/edit params) => (throws)))

   (context "when the activity is not editable"
     (let [activity (mock/there-is-an-activity)
           actor (mock/a-user-exists)
           params {:_id activity}]
       (session/with-user actor
         (actions.activity/edit params) => (throws))))

   (context "when the provided params contain invalid keys"
     (let [actor (mock/a-user-exists)
           activity (mock/there-is-an-activity {:user actor})
           params {:_id (:_id activity)
                   :author "acct:foo@bar.baz"}]
       (session/with-user actor
         (actions.activity/edit params) => (throws))))

   (context "when the params are valid"
     (let [actor (mock/a-user-exists)
           activity (mock/there-is-an-activity {:user actor})
           params {:_id (:_id activity)
                   :title (fseq :title)
                   :nsfw true}]
       (session/with-user actor
         (actions.activity/edit params) => (partial instance? Activity)
         (let [edited-item (model.activity/fetch-by-id (:_id activity))]
           (:title edited-item) => (:title params)
           (:nsfw edited-item) => true))))
   )

 (context #'actions.activity/oembed
   (with-context [:http :html]
     (let [activity (mock/there-is-an-activity)]
       (actions.activity/oembed activity) =>
       (check [response]
         response => map?
         (:html response) => string?))))

 (context #'actions.activity/fetch-by-conversation
   (context "when there are matching activities"
     (let [conversation (mock/a-conversation-exists)
           activity (mock/there-is-an-activity {:conversation conversation})]
       (actions.activity/fetch-by-conversation conversation) =>
       (check [response]
         (count (:items response)) => 1))))

 (context #'actions.activity/fetch-by-conversations
   (context "when there are matching activities"
     (let [conversation1 (mock/a-conversation-exists)
           conversation2 (mock/a-conversation-exists)
           activity1 (mock/there-is-an-activity {:conversation conversation1})
           activity2 (mock/there-is-an-activity {:conversation conversation2})]
       (actions.activity/fetch-by-conversations [(:_id conversation1) (:_id conversation2)]) =>
       (check [response]
         (count (:items response)) => 2))))
 )
