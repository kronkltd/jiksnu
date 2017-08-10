(ns jiksnu.helpers.response
  (:require [jiksnu.helpers.actions :refer [current-page]]
            [midje.sweet :refer :all]))

(defmacro check-response
  [& body]
  `(try+ (and (not (fact ~@body))
              (throw+ "failed"))
         (catch RuntimeException ex#
           (.printStackTrace ex#)
           (throw+ ex#))))

(defn response-should-be-redirect
  []
  (check-response
   (:status @current-page) => #(<= 300 %)
   (:status @current-page) => #(> 400 %)))

(defn response-should-be-sucsessful
  []
  (check-response
   (:status @current-page) => 200))
