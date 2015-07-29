(ns jiksnu.modules.web.filters.user-filters-test
  (:require [ciste.core :refer [with-serialization with-format
                                *serialization* *format*]]
            [ciste.filters :refer [filter-action]]
            [clojure.tools.logging :as log]
            [jiksnu.actions.user-actions :as actions.user]
            jiksnu.modules.web.filters.user-filters
            [jiksnu.test-helper :as th]
            [midje.sweet :refer :all]
            [ring.mock.request :as req]))


(namespace-state-changes
 [(before :contents (th/setup-testing))
  (after :contents (th/stop-testing))])

(fact "filter-action #'index :http"
  (let [action #'actions.user/index]
    (with-serialization :http
      (let [request {}]
        (filter-action action request) => map?))))


