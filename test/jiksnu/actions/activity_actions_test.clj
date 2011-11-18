(ns jiksnu.actions.activity-actions-test
  (:use (ciste [core :only [with-context]]
               [debug :only [spy]])
        ciste.sections.default
        (clj-factory [core :only [factory]])
        clojure.test
        jiksnu.actions.activity-actions
        (jiksnu core-test
                [model :only [activity?]]
                [session :only [with-user]])
        midje.sweet)
  (:require (jiksnu [abdera :as abdera])
            (jiksnu.actions [user-actions :as actions.user])
            (jiksnu.model [activity :as model.activity]
                          [user :as model.user])
            (jiksnu.sections activity-sections))
  (:import jiksnu.model.Activity
           jiksnu.model.User))

(use-fixtures :once test-environment-fixture)

(deftest test-set-recipients
  (fact "should return an activity with the recipients added"
    (let [activity (factory Activity)]
      (set-recipients activity) => activity?)))

(deftest test-entry->activity
  (against-background
    [(around :facts
       (let [user (actions.user/create (factory User))]
         ?form))]
    
    ;; TODO: Load elements from resources
    (fact "should return an Activity"
      (with-context [:http :atom]
        (let [entry (show-section (factory Activity {:author (:_id user)}))]
          (entry->activity (spy entry)) => activity?)))

    #_(testing "when coming from an identi.ca feed"
      (against-background
        [(around :facts
           (let [feed nil #_(abdera/load-file "identica-update.xml")
                 entry (first (abdera/get-entries feed))]
             ?form))]
        
        (fact "should parse the published field"
          (entry->activity entry) => activity?
          (provided
            (.getId entry) => "1"
            )
      
         ))))
  )

(deftest test-prepare-activity
  (fact "should return an activity"
    (let [user (model.user/create (factory User))]
      (with-user user
        (let [args (factory Activity)]
          (prepare-activity args) => activity?)))))

(deftest test-create
  (testing "when the user is logged in"
    (testing "and it is a valid activity"
     (fact "should return that activity"
       (let [user (actions.user/create (factory User))]
         (with-user user
           (let [activity (factory Activity)]
             (create activity) => activity?)))))))

(deftest test-post
  ;; TODO: Move this to 'post'
  (testing "when the user is not logged in"
    (facts "should return nil"
      (let [activity (factory Activity)]
        (post activity) => nil))))

(deftest test-delete
  (testing "when the activity exists"
    (testing "and the user owns the activity"
      (fact "should delete that activity"
        (let [user (model.user/create (factory User))]
          (with-user user
            (let [activity (create (factory Activity {:author (:_id user)}))]
              (delete activity)
              (model.activity/fetch-by-id (:_id activity)) => nil)))))
    (testing "and the user does not own the activity"
      (fact "should not delete that activity"
        (let [user1 (model.user/create (factory User))
              user2 (model.user/create (factory User))
              activity (with-user user1
                         (model.activity/create (factory Activity)))]
          (with-user user2
            (delete activity)
            (model.activity/fetch-by-id (:_id activity)) => activity?))))))

(deftest test-edit)

(deftest test-new)

(deftest test-show
  (testing "when the record exists"
    (testing "and the user is not logged in"
      (testing "and the record is public"
        (facts "should return the activity"
          (let [author (model.user/create (factory User))
                activity (with-user author
                           (create (factory Activity)))]
            (show (:_id activity)) => activity?)))
      (testing "and the record is not public"
        (facts "should return nil"
          (let [author (model.user/create (factory User))
                activity (with-user author
                           (create (factory Activity {:public false})))]
            (show (:_id activity)) => nil?))))
    (testing "and the user is logged in"
      (testing "and is the author"
        (facts "should return the activity"
          (let [user (model.user/create (factory User))]
            (with-user user
              (let [activity (create (factory Activity))]
                (show (:_id activity)) => activity?)))))
      (testing "and is not the author"
        (testing "and is not on the access list"
          (testing "and is an admin"
            (facts "should return the activity"
              (let [user (model.user/create (factory User {:admin true}))
                    author (model.user/create (factory User))]
                (let [activity (with-user author
                                 (create (factory Activity {:public false})))]
                  (with-user user
                    (show (:_id activity)) => activity?)))))
          (testing "and is not an admin"
            (facts "should return nil"
              (let [user (model.user/create (factory User))
                    author (model.user/create (factory User))
                    activity (with-user author
                               (create (factory Activity {:public false})))]
                (with-user user
                  (show (:_id activity)) => nil?)))))))
    (testing "and the record is not public"
      (testing "and the user is not logged in"
        (facts "should return nil"
          (let [activity (create (factory Activity {:public false}))]
            (show (:_id activity)) => nil))))))

(deftest test-update)
