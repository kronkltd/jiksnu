(ns jiksnu.core-test
  (:use ciste.config
        clojure.test
        jiksnu.model))

(defn test-environment-fixture
  [f]
  (load-config)
  (with-environment :test (f)))

