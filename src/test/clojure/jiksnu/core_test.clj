(ns jiksnu.core-test
  (:use clojure.test
        jiksnu.model))

(defn test-environment-fixture
  [f] (with-environment :test (f)))


(use-fixtures :each test-environment-fixture)


