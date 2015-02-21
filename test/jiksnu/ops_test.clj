(ns jiksnu.ops-test
  (:require [clj-factory.core :refer [factory fseq]]
            [clojure.tools.logging :as log]
            [jiksnu.mock :as mock]
            [jiksnu.model :as model]
            [jiksnu.ops :as ops]
            [jiksnu.test-helper :as th]
            [jiksnu.transforms.user-transforms :as transforms.user]
            [midje.sweet :refer :all])
  (:import jiksnu.model.Domain
           jiksnu.model.Resource))

(namespace-state-changes
 [(before :contents (th/setup-testing))
  (after :contents (th/stop-testing))])

(def test-url "http://www.example.com/")

(fact "#'ops/get-discovered"
  (let [domain (mock/a-domain-exists)]
    @(ops/get-discovered domain) => #(instance? Domain %)))

(future-fact "#'ops/update-resource"
  @(ops/update-resource test-url) => #(instance? Resource %))
