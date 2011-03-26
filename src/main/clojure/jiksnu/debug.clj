(ns jiksnu.debug
  (:use clojure.pprint))

(defmacro spy
  [sym]
  `(let [value# ~sym]
     (print (str ~(str sym) ": "))
     (pprint value#)
     value#))

;; (defn spy
;;   [variable]
;;   (println (str (name variable) ": " variable)))

