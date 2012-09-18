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
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.features-helper :as feature]
            [jiksnu.model :as model]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.domain :as model.domain]
            [jiksnu.model.user :as model.user]))


(test-environment-fixture
 
 (fact "#'set-recipients"
   (fact "when there are no recipient uris"
     (fact "should return that activity"
       (let [activity (factory :activity)]
         (set-recipients activity) => activity)))
   (fact "When the activity contains a recipient uri"
     (let [recipient (feature/a-user-exists)
           activity (factory :activity {:recipient-uris [(:id recipient)]})]
       (set-recipients activity) =>
       (every-checker
        #(= (:_id recipient) (first (:recipients %)))))))

 (fact "#'oembed->activity"
   (let [oembed-str (slurp "test-resources/oembed.json")]
     ;; TODO: complete
     oembed-str => string?))
 
 (fact "entry->activity"
   (let [domain-name (fseq :domain)
         domain (actions.domain/find-or-create (factory :domain
                                                        {:discovered true
                                                         :links [{:rel "lrdd"
                                                                  :template (str "http://" domain-name "/lrdd?uri={uri}")}]
                                                         :_id domain-name}))
         user (feature/a-user-exists {:domain domain})]

     ;; TODO: Load elements from resources
     (fact "should return an Activity"
       (with-context [:http :atom]
         (let [entry (show-section (model/map->Activity
                                    (factory :activity {:author (:_id user)})))]
           (entry->activity entry) => model/activity?)))
     
     (future-fact "when coming from an identi.ca feed"
       (fact "should parse the published field"
         (let [feed (abdera/load-file "identica-update.xml")
               entry (first (abdera/get-entries feed))]
           (entry->activity entry) => model/activity?)))))

 (fact "#'find-by-user"
   (fact "when the user has activities"
     (model/drop-all!)
     (let [user (feature/a-user-exists)
           activity (feature/there-is-an-activity {:user user})]
       (find-by-user user) =>
       (every-checker
        map?
        (fn [response]
          (fact
            (:total-records response) => 1
            (count (:items response)) => 1
            (:items response) => (partial every? model/activity?)))))))

 (fact "#'create"
   (fact "when the user is logged in"
     (fact "and it is a valid activity"
       (fact "should return that activity"
         (let [feed-source (feature/a-feed-source-exists)
               user (feature/a-user-exists)]
           (let [activity (factory :activity {:author (:_id user)
                                              :update-source (:_id feed-source)
                                              :local true})]
             (create activity) => model/activity?))))))

 (fact "#'post"
   (fact "when the user is not logged in"
     (let [activity (dissoc (factory :activity) :author)]
       (post activity) => (throws RuntimeException))))

 (fact "#'delete"
   (fact "when the activity exists"
     (fact "and the user owns the activity"
       (fact "should delete that activity"
         (let [user (feature/a-user-exists)]
           (with-user user
             (let [activity (feature/there-is-an-activity {:user user})]
               (delete activity) => activity
               (model.activity/fetch-by-id (:_id activity)) => nil)))))
     (fact "and the user does not own the activity"
       (fact "should not delete that activity"
         (let [user (feature/a-user-exists)
               author (feature/another-user-exists)
               activity (feature/there-is-an-activity {:user author})]
           (with-user user
             (delete activity) => (throws RuntimeException)
             (model.activity/fetch-by-id (:_id activity)) => activity))))))

 (fact "#'viewable?"
   (fact "When it is public"
     (let [activity (feature/there-is-an-activity)]
       (viewable? activity .user.)) => truthy)
   (fact "when it is not public"
     (fact "when the user is the author"
       (let [user (feature/a-user-exists)
             activity (feature/there-is-an-activity {:user user})]
         (viewable? activity user)) => truthy)
     (fact "when the user is not the author"
       (fact "when the user is an admin"
         (let [user (feature/a-user-exists {:admin true})
               activity (feature/there-is-an-activity {:modifier "private"})]
           (viewable? activity user)) => truthy)
       (fact "when the user is not an admin"
         (let [user (feature/a-user-exists)
               author (feature/a-user-exists)
               activity (feature/there-is-an-activity {:modifier "private"
                                                       :user author})]
           (viewable? activity user)) => falsey))))
 
 (fact "#'show"
   (fact "when the record exists"
     (fact "and the record is viewable"
       (let [activity (feature/there-is-an-activity)]
         (show activity) => activity
         (provided
          (viewable? activity) => true)))
     (fact "and the record is not viewable"
       (let [activity (feature/there-is-an-activity)]
         (show activity) => (throws RuntimeException)
         (provided
          (viewable? activity) => false)))))

 (fact "#'oembed"
   (with-context [:http :html]
     (let [activity (feature/there-is-an-activity)]
       (oembed activity) =>
       (every-checker
        map?
        (comp string? :html)))))
 )
