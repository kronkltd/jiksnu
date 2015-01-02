(ns jiksnu.modules.web.views.conversation-views-test
  (:use [ciste.core :only [with-context with-format with-serialization]]
        [ciste.filters :only [filter-action]]
        [ciste.views :only [apply-view]]
        [clj-factory.core :only [factory]]
        [jiksnu.actions.conversation-actions :only [index]]
        [jiksnu.test-helper :only [check hiccup->doc test-environment-fixture]]
        [jiksnu.ko :only [*dynamic*]]
        [midje.sweet :only [=> fact]])
  (:require [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.model :as model]
            [jiksnu.model.domain :as model.domain]
            [jiksnu.model.user :as model.user]
            jiksnu.modules.web.views.conversation-views)
  (:import java.io.StringReader
           jiksnu.model.User))

(test-environment-fixture

 (fact "apply-view #'index"
   (let [action #'index]

     (fact "when the serialization is :http"
       (with-serialization :http

         (fact "when the format is :html"
           (with-format :html
             (fact "when the request is not dynamic"
               (binding [*dynamic* false]
                 (fact "when there are no conversations"
                   (let [request {:action action}
                         response (filter-action action request)]
                     (apply-view request response) => map?))))))

         (fact "when the format is :viewmodel"
           (with-format :viewmodel
             (fact "when the request is not dynamic"
               (binding [*dynamic* false]
                 (fact "when there are no conversations"
                   (let [request {:action action
                                  :format :viewmodel
                                  :params {:format :viewmodel}}
                         response (filter-action action request)]
                     (apply-view request response) => map?))))))
         ))
     ))

)
