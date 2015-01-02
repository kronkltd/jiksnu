(ns jiksnu.modules.web.views.conversation-views-test
  (:require [ciste.core :refer [with-context]]
            [ciste.filters :refer [filter-action]]
            [ciste.views :refer [apply-view]]
            [clj-factory.core :refer [factory]]
            [jiksnu.actions.conversation-actions :refer [index]]
            [jiksnu.test-helper :refer [hiccup->doc test-environment-fixture]]
            [midje.sweet :refer [=> fact]]))

(test-environment-fixture

 (fact "apply-view #'index [:http :viewmodel]"
   (let [action #'index]
     (with-context [:http :viewmodel]
       (fact "when there are no conversations"
         (let [request {:action action
                        :format :viewmodel
                        :params {:format :viewmodel}}
               response (filter-action action request)
               rendered (apply-view request response)]

           (fact "returns a map"
             rendered => map?)))
       )
     ))

 )
