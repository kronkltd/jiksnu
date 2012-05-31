(ns jiksnu.views.stream-views-test
  (:use (ciste [config :only [with-environment]]
               core
               [debug :only [spy]]
               sections views)
        clj-factory.core
        (jiksnu [session :only [with-user]]
                test-helper)
        jiksnu.actions.stream-actions
        jiksnu.views.stream-views
        midje.sweet)
  (:require (clojure [string :as string])
            (hiccup [core :as h])
            (jiksnu [abdera :as abdera])
            (jiksnu.model [activity :as model.activity]
                          [user :as model.user]))
  (:import org.apache.abdera2.model.Entry
           jiksnu.model.Activity
           jiksnu.model.User))

(test-environment-fixture

 (fact "apply-view #'public-timeline :atom"
   (fact "should be a map"
     (with-context [:http :atom]
       (with-user (model.user/create (factory User))
         (let [activity (model.activity/create (factory Activity))]
           (apply-view {:action #'public-timeline :format :atom}
                       [[activity] {}]) =>
                       (every-checker
                        map?
                        #(not (:template %))
                        (fn [response]
                          (let [feed (abdera/parse-xml-string (:body response))]
                            (= 1
                               (count (.getEntries feed))))))))))))
