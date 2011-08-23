(ns jiksnu.redis-test
  (:use ciste.debug
        clojure.test
        midje.sweet
        clj-factory.core
        jiksnu.model
        jiksnu.redis))

(background
 (around :facts
   (with-environment :test
     (init-client)
     ?form)))

(deftest test-sadd
  (fact "should add the value to the set at the key"
    (let [key "test.set"
          value (fseq :word)]
      (client [:del key])
      @(sadd key value) => 1)))
