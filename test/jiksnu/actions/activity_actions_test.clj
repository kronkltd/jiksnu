(ns jiksnu.actions.activity-actions-test
  (:use (ciste [config :only [with-environment]]
               [core :only [with-context]]
               [debug :only [spy]])
        ciste.sections.default
        (clj-factory [core :only [factory fseq]])
        jiksnu.actions.activity-actions
        (jiksnu test-helper
                [model :only [activity?]]
                [session :only [with-user]])
        midje.sweet)
  (:require (jiksnu [abdera :as abdera])
            (jiksnu.model [activity :as model.activity]
                          [domain :as model.domain]
                          [user :as model.user])
            (jiksnu.sections activity-sections))
  (:import jiksnu.model.Activity
           jiksnu.model.Domain
           jiksnu.model.User))


(test-environment-fixture
 
 (fact "set-recipients"
   (fact "should return an activity with the recipients added"
     (let [activity (factory Activity)]
       (set-recipients activity) => activity?)))

 (fact "entry->activity"
   (let [domain-name (fseq :domain)
         domain (model.domain/create (factory Domain
                                              {:discovered true
                                               :links [{:rel "lrdd"
                                                        :template (str "http://" domain-name "/lrdd?uri={uri}")}]
                                               :_id domain-name}))
         user (model.user/create (factory User {:domain domain-name}))]

     ;; TODO: Load elements from resources
     (fact "should return an Activity"
       (with-context [:http :atom]
         (let [entry (show-section (factory Activity {:author (:_id user)}))]
           (entry->activity entry) => activity?)))
     
     (future-fact "when coming from an identi.ca feed"
       (fact "should parse the published field"
         (let [feed nil #_(abdera/load-file "identica-update.xml")
               entry (first (abdera/get-entries feed))]
           (entry->activity entry) => activity?
           #_(provided
               (.getId entry) => "1"))))
     ))

 (fact "#'create"
   (fact "when the user is logged in"
     (fact "and it is a valid activity"
       (fact "should return that activity"
         (let [user (model.user/create (factory User))]
           (with-user user
             (let [activity (factory Activity)]
               (create activity) => activity?)))))))

 (fact "#'post"
   (fact "when the user is not logged in"
     (fact "should return nil"
       (let [activity (factory Activity)]
         (post activity) => nil))))

 (fact "#'delete"
   (fact "when the activity exists"
     (fact "and the user owns the activity"
       (fact "should delete that activity"
         (let [user (model.user/create (factory User))]
           (with-user user
             (let [activity (create (factory Activity {:author (:_id user)}))]
               (delete activity)
               (model.activity/fetch-by-id (:_id activity)) => nil)))))
     (fact "and the user does not own the activity"
       (fact "should not delete that activity"
         (let [user1 (model.user/create (factory User))
               user2 (model.user/create (factory User))
               activity (with-user user1
                          (model.activity/create (factory Activity)))]
           (with-user user2
             (delete activity) => (throws RuntimeException)
             (model.activity/fetch-by-id (:_id activity)) => activity?))))))

 (fact "#'show"
   (fact "when the record exists"
     (fact "and the user is not logged in"
       (fact "and the record is public"
         (facts "should return the activity"
           (let [author (model.user/create (factory User))
                 activity (with-user author
                            (create (factory Activity)))]
             (show activity) => activity?)))
       (fact "and the record is not public"
         (facts "should return nil"
           (let [author (model.user/create (factory User))
                 activity (with-user author
                            (create (factory Activity {:public false})))]
             (show activity) => nil?))))
     (fact "and the user is logged in"
       (fact "and is the author"
         (facts "should return the activity"
           (let [user (model.user/create (factory User))]
             (with-user user
               (let [activity (create (factory Activity))]
                 (show activity) => activity?)))))
       (fact "and is not the author"
         (fact "and is not on the access list"
           (fact "and is an admin"
             (facts "should return the activity"
               (let [user (model.user/create (factory User {:admin true}))
                     author (model.user/create (factory User))]
                 (let [activity (with-user author
                                  (create (factory Activity {:public false})))]
                   (with-user user
                     (show activity) => activity?)))))
           (fact "and is not an admin"
             (facts "should return nil"
               (let [user (model.user/create (factory User))
                     author (model.user/create (factory User))
                     activity (with-user author
                                (create (factory Activity {:public false})))]
                 (with-user user
                   (show activity) => nil?)))))))
     (fact "and the record is not public"
       (fact "and the user is not logged in"
         (facts "should return nil"
           (let [activity (create (factory Activity {:public false}))]
             (show activity) => nil))))))
 )
