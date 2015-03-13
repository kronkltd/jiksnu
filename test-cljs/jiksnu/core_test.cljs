(ns jiksnu.core-test
  (:require purnam.test)
  (:use-macros [purnam.test :only [describe it is is-not]])
)

(describe "foo"
          (it "FIX THIS: One Plus One Equals... "
              (is (+ 1 1) 2))
)

