(ns jiksnu.views.stream-views-test
  (:use [ciste.config :only [with-environment]]
        ciste.core
        ciste.sections
        ciste.views
        clj-factory.core
        [jiksnu.session :only [with-user]]
        jiksnu.test-helper
        jiksnu.actions.stream-actions
        jiksnu.views.stream-views
        midje.sweet)
  (:require [clojure.string :as string]
            [clojure.tools.logging :as log]
            [hiccup.core :as h]
            [jiksnu.abdera :as abdera]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.user :as model.user])
  (:import org.apache.abdera2.model.Entry
           jiksnu.model.Activity
           jiksnu.model.User))

(test-environment-fixture

 (fact "apply-view #'public-timeline :atom"
   (with-context [:http :atom]
     (with-user (model.user/create (factory :local-user))
       (let [action #'public-timeline
             activity (model.activity/create (factory :activity))
             request {:action action :format :atom}
             response (filter-action action request)]
         (apply-view request response) =>
         (every-checker
          map?
          #(not (:template %))
          (fn [response]
            (let [feed (abdera/parse-xml-string (:body response))]
              (= 1
                 (count (.getEntries feed)))))))))))
