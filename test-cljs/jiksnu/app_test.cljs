(ns jiksnu.app-test
  (:require jiksnu.app
            purnam.test)
  (:use-macros
   [gyr.test :only [describe.controller]]
   [purnam.test :only [it is is-not describe]]

               ))

(def a (atom 0))

(describe
 {:doc "simple test"}
 ;; (js/console.log "a" @a)
 (it "does the needful"
     (is (+ 2 2)  4))
 )
