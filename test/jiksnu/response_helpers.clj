(ns jiksnu.response-helpers
  (:require [jiksnu.action-helpers :refer [current-page check-response]]
            [midje.sweet :refer [=>]]))

(defn response-should-be-redirect
  []
  (check-response
   (:status @current-page) => #(<= 300 %)
   (:status @current-page) => #(> 400 %)))

(defn response-should-be-sucsessful
  []
  (check-response
   (:status @current-page) => 200))

