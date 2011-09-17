(ns jiksnu.helpers.user-helpers-test
  (:use clj-factory.core
        clojure.test
        midje.sweet
        (jiksnu core-test model)
        jiksnu.helpers.user-helpers)
  (:import jiksnu.model.User))

(use-fixtures :each test-environment-fixture)

(background
 (around :facts
   (with-environment :test
     (let [actor (factory User)]
       ?form))))
