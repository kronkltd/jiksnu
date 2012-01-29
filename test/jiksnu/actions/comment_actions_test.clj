(ns jiksnu.actions.comment-actions-test
  (:use (ciste [config :only [with-environment]])
        (clj-factory [core :only [factory]])
        clojure.test
        midje.sweet
        (jiksnu test-helper
                [session :only (with-user)])
        jiksnu.actions.comment-actions)
  (:require (jiksnu.actions [activity-actions :as actions.activity])
            (jiksnu.model [user :as model.user]))
  (:import jiksnu.model.Activity
           jiksnu.model.User))


(with-environment :test
  (test-environment-fixture)
  ;; (deftest fetch-comments-test)

 (fact "when the activity exists"
   (fact "and there are no comments"
     (fact "should return an empty sequence"
       (let [actor (model.user/create (factory User))]
         (with-user actor
           (let [activity (actions.activity/create (factory Activity))
                 [_ comments] (fetch-comments activity)]
             comments => empty?))))))
)
