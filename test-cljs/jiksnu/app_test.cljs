(ns jiksnu.app-test
  (:require jiksnu.app
            purnam.test)
  (:use-macros [purnam.test :only [beforeEach describe fact it is is-not]]))

(def a (atom 0))

(beforeEach
 (.log js/console "before")
 (swap! a inc))

(describe "foo"
  (it "FIX THIS: One Plus One Equals... "
    (is (+ @a 1) 2)
    (is (+ 2 2) 4)))

(fact
 (+ @a 2) => 4
)
