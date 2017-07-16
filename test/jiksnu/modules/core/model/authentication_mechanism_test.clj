(ns jiksnu.modules.core.model.authentication-mechanism-test
  (:require [jiksnu.modules.core.model.like :refer [fetch-all]]
            [jiksnu.test-helper :as th]
            [midje.sweet :refer :all]))

(th/module-test ["jiksnu.modules.core"])

(facts "#'fetch-all"
  (fact "when not given any parameters"
    (fetch-all) => seq?)

  (fact "when passed an empty map"
    (fetch-all {}) => seq?))
