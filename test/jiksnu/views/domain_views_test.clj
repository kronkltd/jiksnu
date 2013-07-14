(ns jiksnu.views.domain-views-test
  (:use [ciste.core :only [with-serialization with-format]]
        [ciste.filters :only [filter-action]]
        [ciste.views :only [apply-view]]
        [clj-factory.core :only [factory]]
        [jiksnu.test-helper :only [test-environment-fixture]]
        [midje.sweet :only [contains =>]])
  (:require [clojure.tools.logging :as log]
            [hiccup.core :as h]
            [jiksnu.actions.domain-actions :as actions.domain]
            [jiksnu.ko :as ko]
            [jiksnu.mock :as mock]
            [jiksnu.model :as model]
            [jiksnu.model.domain :as model.domain]
            [jiksnu.model.user :as model.user]
            [jiksnu.actions.user-actions :as actions.user])
  (:import jiksnu.model.User))


(test-environment-fixture

 (context "apply-view #'show"
   (let [action #'jiksnu.actions.domain-actions/show]

     (context "when the serialization is :http"
       (with-serialization :http

         (context "when the format is :html"
           (with-format :html

             (context "when dynamic is false"
               (binding [ko/*dynamic* false]

                 (let [domain (mock/a-domain-exists)
                       user (mock/a-user-exists {:domain domain})]

                   (let [request {:action action
                                  :params {:id (:_id domain)}}
                         response (filter-action action request)]
                     (apply-view request response) =>
                     (chek [response]
                       response => map?
                       (let [body (h/html (:body response))]
                         body => (re-pattern (str (:_id domain)))))))))
             ))
         ))
     ))
 )
