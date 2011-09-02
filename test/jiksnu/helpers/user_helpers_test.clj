(ns jiksnu.helpers.user-helpers-test
  (:use clj-factory.core
        clojure.test
        midje.sweet
        jiksnu.helpers.user-helpers
        jiksnu.model)
  (:import jiksnu.model.User))



(deftest test-fetch-user-meta
  (fact
    (let [user (factory User)]
      (fetch-user-meta user)) => nil
    )
  )
