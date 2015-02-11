(ns jiksnu.modules.web.filters.like-filters-test
  (:require [ciste.core :refer [with-serialization with-format
                                *serialization* *format*]]
            [ciste.filters :refer [filter-action]]
            [clojure.tools.logging :as log]
            [jiksnu.actions.like-actions :as actions.like]
            [jiksnu.model :as model]
            [jiksnu.model.like :as model.like]
            [jiksnu.test-helper :as th]
            [jiksnu.util :as util]
            [midje.sweet :refer :all]
            [ring.mock.request :as req]))

(namespace-state-changes
 [(before :contents (th/setup-testing))
  (after :contents (th/stop-testing))])

(fact "filter-action #'actions.like/delete"
  (let [action #'actions.like/delete]
    (fact "when the serialization is :http"
      (with-serialization :http
        (let [request {:params {:id .id.}}]
          (filter-action action request) => .response.
          (provided
            (model.like/fetch-by-id .id.) => .like.
            (actions.like/delete .like.) => .response.))))))


