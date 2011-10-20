(ns jiksnu.core-test
  (:use (ciste [config :only [load-config with-environment]]
               [triggers :only [*thread-pool*]])
        clojure.test
        jiksnu.model)
  (:require (karras [entity :as entity]))
  (:import (jiksnu.model Person Activity Subscription User Item Domain
                         PushSubscription MagicKeyPair)))

(defn test-environment-fixture
  [f]
  (load-config)
  (with-environment :test
    (with-database
      (drop-all!)
      (f)
      (.shutdown @*thread-pool*))))
