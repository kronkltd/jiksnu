(ns jiksnu.modules.web.views.user-views-test
  (:require [ciste.core :refer [with-format with-serialization]]
            [ciste.filters :refer [filter-action]]
            [ciste.views :refer [apply-view]]
            [clojure.tools.logging :as log]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.ko :refer [*dynamic*]]
            [jiksnu.mock :as mock]
            [jiksnu.model :as model]
            [jiksnu.model.domain :as model.domain]
            [jiksnu.model.user :as model.user]
            jiksnu.modules.web.views.user-views
            [jiksnu.test-helper :refer [check context future-context
                                        test-environment-fixture]]

            [midje.sweet :refer [contains =>]]))

(test-environment-fixture

 (context "apply-view #'actions.user/index"
   (let [action #'actions.user/index]
     (context "when the serialization is :http"
       (with-serialization :http
         (context "when the format is :html"
           (with-format :html
             (context "when the request is not dynamic"
               (binding [*dynamic* false]
                 (context "when there are no activities"
                   (let [request {:action action}
                         response (filter-action action request)]
                     (apply-view request response) => map?))))))))))

 )
