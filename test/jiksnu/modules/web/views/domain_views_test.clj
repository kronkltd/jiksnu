(ns jiksnu.modules.web.views.domain-views-test
  (:require [ciste.core :refer [with-serialization with-format]]
            [ciste.filters :refer [filter-action]]
            [ciste.views :refer [apply-view]]
            [clj-factory.core :refer [factory]]
            [clojure.tools.logging :as log]
            [jiksnu.actions.domain-actions :as actions.domain]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.test-helper :refer [context future-context test-environment-fixture]]
            [midje.sweet :refer [=> fact]])
  (:import jiksnu.model.Domain))


(test-environment-fixture

 ;; TODO: going away
 (context "apply-view #'show"
   (let [action #'jiksnu.actions.domain-actions/show]

     (context "when the serialization is :http"
       (with-serialization :http

         (context "when the format is :html"
           (with-format :html

             (let [domain (Domain.)
                   request {:action action
                            :params {:id (:_id domain)}}
                   response (filter-action action request)
                   rendered (apply-view request response)]

               (fact "returns a map"
                 rendered => map?)
               )
             ))
         ))
     ))
 )
