(ns jiksnu.response-helpers
  (:use [jiksnu.action-helpers :only [current-page check-response]]
        [midje.sweet :only [=>]]))

(defn response-should-be-redirect
  []
  (check-response
   (:status @current-page) => #(<= 300 %)
   (:status @current-page) => #(> 400 %)))

(defn response-should-be-sucsessful
  []
  (check-response
   (:status @current-page) => 200))

