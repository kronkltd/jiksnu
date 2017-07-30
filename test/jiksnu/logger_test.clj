(ns jiksnu.logger-test
  (:require [jiksnu.logger :as sut]
            [midje.sweet :refer :all]))

(facts "#'jiksnu.logger/set-logger"
  (fact "returns a map"
    (sut/set-logger) => map?))
