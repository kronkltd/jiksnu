(ns jiksnu.actions.activity-actions-test
  (:use [ciste.core :only [with-context]]
        [ciste.sections.default :only [show-section]]
        [clj-factory.core :only [factory fseq]]
        jiksnu.actions.activity-actions
        [jiksnu.test-helper :only [test-environment-fixture]]
        [jiksnu.session :only [with-user]]
        [midje.sweet :only [fact future-fact => every-checker throws truthy falsey]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.abdera :as abdera]
            [jiksnu.actions.domain-actions :as actions.domain]
            [jiksnu.db :as db]
            [jiksnu.mock :as mock]
            [jiksnu.model :as model]
            [jiksnu.model.activity :as model.activity])
  (:import jiksnu.model.Activity))

(test-environment-fixture

 (fact "#'oembed->activity"
   (let [oembed-str (slurp "test-resources/oembed.json")]
     ;; TODO: complete
     oembed-str => string?))

 (fact "entry->activity"
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
     (fact "should return an Activity"
       (with-context [:http :atom]
         (let [activity (mock/there-is-an-activity {:user user})
               entry (show-section activity)]
           (entry->activity entry) => (partial instance? Activity))))

     (future-fact "when coming from an identi.ca feed"
       (fact "should parse the published field"
         (let [feed (abdera/load-file "identica-update.xml")
               entry (first (abdera/get-entries feed))]
           (entry->activity entry) => (partial instance? Activity))))
     ))

 (fact "#'find-by-user"
   (fact "when the user has activities"
     (db/drop-all!)
     (let [user (mock/a-user-exists)
           activity (mock/there-is-an-activity {:user user})]
       (find-by-user user) =>
       (every-checker
        map?
        (fn [response]
          (fact
            (:totalRecords response) => 1
            (count (:items response)) => 1
            (:items response) => (partial every? (partial instance? Activity))))))))

 (fact "#'create"
   (fact "when the user is logged in"
     (fact "and it is a valid activity"
       (fact "should return that activity"
         (let [domain (mock/a-domain-exists)
               feed-source (mock/a-feed-source-exists)
               conversation (mock/a-conversation-exists)
               user (mock/a-user-exists)
               activity (factory :activity {:author        (:_id user)
                                            :conversation  (:_id conversation)
                                            :update-source (:_id feed-source)
                                            :local         true})]
           (create activity) => (partial instance? Activity))))))

 (fact "#'post"
   (fact "when the user is not logged in"
     (let [activity (dissoc (factory :activity) :author)]
       (post activity) => (throws RuntimeException))))

 (fact "#'delete"
   (fact "when the activity exists"
     (fact "and the user owns the activity"
       (fact "should delete that activity"
         (let [user (mock/a-user-exists)]
           (with-user user
             (let [activity (mock/there-is-an-activity {:user user})]
               (delete activity) => activity
               (model.activity/fetch-by-id (:_id activity)) => nil)))))
     (fact "and the user does not own the activity"
       (fact "should not delete that activity"
         (let [user (mock/a-user-exists)
               author (mock/a-remote-user-exists)
               activity (mock/there-is-an-activity {:user author})]
           (with-user user
             (delete activity) => (throws RuntimeException)
             (model.activity/fetch-by-id (:_id activity)) => activity))))))

 (fact "#'viewable?"
   (fact "When it is public"
     (let [activity (mock/there-is-an-activity)]
       (viewable? activity .user.)) => truthy)
   (fact "when it is not public"
     (fact "when the user is the author"
       (let [user (mock/a-user-exists)
             activity (mock/there-is-an-activity {:user user})]
         (viewable? activity user)) => truthy)
     (fact "when the user is not the author"
       (fact "when the user is an admin"
         (let [user (mock/a-user-exists {:admin true})
               activity (mock/there-is-an-activity {:modifier "private"})]
           (viewable? activity user)) => truthy)
       (fact "when the user is not an admin"
         (let [user (mock/a-user-exists)
               author (mock/a-user-exists)
               activity (mock/there-is-an-activity {:modifier "private"
                                                    :user author})]
           (viewable? activity user)) => falsey))))

 (fact "#'show"
   (fact "when the record exists"
     (fact "and the record is viewable"
       (let [activity (mock/there-is-an-activity)]
         (show activity) => activity
         (provided
           (viewable? activity) => true)))
     (fact "and the record is not viewable"
       (let [activity (mock/there-is-an-activity)]
         (show activity) => (throws RuntimeException)
         (provided
           (viewable? activity) => false)))))

 (fact "#'oembed"
   (with-context [:http :html]
     (let [activity (mock/there-is-an-activity)]
       (oembed activity) =>
       (every-checker
        map?
        (comp string? :html)))))

 (fact "#'fetch-by-conversation"
   (fact "when there are matching activities"
     (let [conversation (mock/a-conversation-exists)
           activity (mock/there-is-an-activity {:conversation conversation})]
       (fetch-by-conversation conversation) =>
       (fn [response]
         (fact
           (count (:items response)) => 1)))))

 (fact "#'fetch-by-conversations"
   (fact "when there are matching activities"
     (let [conversation1 (mock/a-conversation-exists)
           conversation2 (mock/a-conversation-exists)
           activity1 (mock/there-is-an-activity {:conversation conversation1})
           activity2 (mock/there-is-an-activity {:conversation conversation2})]
       (fetch-by-conversations [(:_id conversation1) (:_id conversation2)]) =>
       (fn [response]
         (fact
           (count (:items response)) => 2)))))
 )
