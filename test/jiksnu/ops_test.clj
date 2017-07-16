(ns jiksnu.ops-test
  (:require [jiksnu.mock :as mock]
            [jiksnu.ops :as ops]
            [jiksnu.test-helper :as th]
            [midje.sweet :refer :all])
  (:import jiksnu.modules.core.model.Domain
           jiksnu.modules.core.model.Resource))

(def test-url "http://www.example.com/")

(th/module-test ["jiksnu.modules.core"])

(fact "#'ops/get-discovered"
  (let [domain (mock/a-domain-exists)]
    @(ops/get-discovered domain) => #(instance? Domain %)))

(future-fact "#'ops/update-resource"
  @(ops/update-resource test-url) => #(instance? Resource %))
