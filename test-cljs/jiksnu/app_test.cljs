(ns jiksnu.app-test
  (:require jiksnu.app
            purnam.test)
  (:use-macros [purnam.test :only [it is describe]]))

(def a (atom 0))

(describe {:doc "simple test"}
  (it "does the needful"
    (is (+ 2 2)  4)))
