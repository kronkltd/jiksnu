(ns jiksnu.modules.web.views.conversation-views-test
  (:use [ciste.core :only [with-context with-format with-serialization]]
        [ciste.filters :only [filter-action]]
        [ciste.views :only [apply-view]]
        [clj-factory.core :only [factory]]
        [jiksnu.actions.conversation-actions :only [index]]
        [jiksnu.test-helper :only [check context future-context
                                   hiccup->doc test-environment-fixture]]
        [jiksnu.ko :only [*dynamic*]]
        [midje.sweet :only [=>]])
  (:require [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.model :as model]
            [jiksnu.model.domain :as model.domain]
            [jiksnu.model.user :as model.user]
            jiksnu.modules.web.views.conversation-views)
  (:import java.io.StringReader
           jiksnu.model.User))

(test-environment-fixture

 (context "apply-view #'index"
   (let [action #'index]

     (context "when the serialization is :http"
       (with-serialization :http

         (context "when the format is :html"
           (with-format :html
             (context "when the request is not dynamic"
               (binding [*dynamic* false]
                 (context "when there are no conversations"
                   (let [request {:action action}
                         response (filter-action action request)]
                     (apply-view request response) => map?))))))

         (context "when the format is :viewmodel"
           (with-format :viewmodel
             (context "when the request is not dynamic"
               (binding [*dynamic* false]
                 (context "when there are no conversations"
                   (let [request {:action action
                                  :format :viewmodel
                                  :params {:format :viewmodel}}
                         response (filter-action action request)]
                     (apply-view request response) => map?))))))
         ))
     ))

)
