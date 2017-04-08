(ns jiksnu.app-test
  (:require jiksnu.app))

(def a (atom 0))

(js/describe "simple test"
  (fn []
    (js/it "does the needful"
      (fn []
        (-> (js/expect (+ 2 2)) (.toBe 4))))))
