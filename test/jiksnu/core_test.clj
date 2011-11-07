(ns jiksnu.core-test
  (:use (ciste [config :only [with-environment]]
               [triggers :only [*thread-pool*]])
        (jiksnu [model :only [drop-all!]]))
  (:require (karras [entity :as entity])))

(defn test-environment-fixture
  [f]
  (with-environment :test
    (drop-all!)
    (f)
    (.shutdown @*thread-pool*)))
