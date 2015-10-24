(ns jiksnu.app-test
  (:require jiksnu.app
            purnam.test)
  (:use-macros [purnam.test :only [beforeEach describe fact facts]]))

(def a (atom 0))

(describe {:doc "simple test"}
  ;; (js/console.log "a" @a)
  (fact (+ 2 2)  => 4)
  (describe {:doc "updating an atom"}
    (beforeEach
     ;; (js/console.log "before")
     (swap! a inc))
    (fact (+ @a 2) => 3)))

