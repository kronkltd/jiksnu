(ns jiksnu.test-helper
  (:use (ciste [config :only [load-config with-environment]]
               [triggers :only [*thread-pool*]])
        midje.sweet
        (jiksnu [model :only [drop-all!]]))
  (:require (karras [entity :as entity])))

(defmacro test-environment-fixture
  []
  `(background
    (around :facts
      (do (load-config)
          (with-environment :test
            (print ".")
            (drop-all!)
            ?form
            #_(.shutdown @*thread-pool*))))))
