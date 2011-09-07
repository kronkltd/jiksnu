(ns jiksnu.core-test
  (:use ciste.config
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
      (doseq [model [Person Activity Subscription
                     User Item Domain PushSubscription MagicKeyPair]]
        (entity/delete-all model))
      (f))))
