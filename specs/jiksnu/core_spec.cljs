(ns jiksnu.core-spec
  (:require
   ;; jiksnu.app
            purnam.test)
  (:use-macros [purnam.core :only [?>]]
               [purnam.test :only [beforeEach describe fact it is is-not]]))


(describe "foo"
  (it "FIX THIS: One Plus One Equals... "
    (.get js/browser "http://localhost:8082/")
    (is (+ 1 1) 2)
    (is (+ 2 2) 4)))

(fact
 (.get js/browser "http://localhost:8082/")
 (.getTitle js/browser) => "Renfer.namey"
)
