(ns jiksnu.ops-test
  (:require [clj-factory.core :refer [factory fseq]]
            [clojure.tools.logging :as log]
            [jiksnu.actions.domain-actions :as actions.domain]
            [jiksnu.mock :as mock]
            [jiksnu.model :as model]
            [jiksnu.ops :as ops]
            [jiksnu.test-helper :as th]
            [jiksnu.transforms.user-transforms :as transforms.user]
            [midje.sweet :refer :all])
  (:import jiksnu.model.Domain))


(namespace-state-changes
 [(before :contents (th/setup-testing))
  (after :contents (th/stop-testing))])



;; (fact #'ops/get-discovered
;;   (let [domain (mock/a-domain-exists)]
;;     (ops/get-discovered domain) =>
;;     (th/check [response]
;;       @response => (partial instance? Domain)
;;       )
;;     )
;;   )


