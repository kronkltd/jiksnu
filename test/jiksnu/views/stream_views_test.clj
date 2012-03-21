(ns jiksnu.views.stream-views-test
  (:use (ciste [config :only [with-environment]]
               core sections views)
        clj-factory.core
        (jiksnu test-helper)
        jiksnu.actions.stream-actions
        jiksnu.views.stream-views
        midje.sweet)
  (:require (jiksnu.model [activity :as model.activity]
                          [user :as model.user]))
  (:import org.apache.abdera2.model.Entry
           jiksnu.model.Activity
           jiksnu.model.User))

(test-environment-fixture

 (fact "apply-view #'index :atom"
   (fact "should be a map"
     (with-context [:http :atom]
       (with-user (model.user/create (factory User))
         (let [activity (model.activity/create (factory Activity))]
           (apply-view {:action #'index :format :atom} [activity]) => map?))))))
