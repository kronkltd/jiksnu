(ns jiksnu.redis-test
  (:use ciste.debug
        clojure.test
        midje.sweet
        clj-factory.core
        (jiksnu core-test model redis)))

(use-fixtures :once test-environment-fixture)

(deftest test-sadd
  (fact "should add the value to the set at the key"
    (let [key "test.set"
          value (fseq :word)]
      (client [:del key])
      @(sadd key value) => 1)))
