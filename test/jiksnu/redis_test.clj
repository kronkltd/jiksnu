(ns jiksnu.redis-test
  (:use (ciste [config :only [with-environment]]
               [debug :only [spy]])
        clojure.test
        midje.sweet
        clj-factory.core
        (jiksnu test-helper model redis)))

(with-environment :test
  (test-environment-fixture)

  (fact "sadd"
    (fact "should add the value to the set at the key"
      (let [key "test.set"
            value (fseq :word)]
        (client [:del key])
        @(sadd key value) => 1))))
