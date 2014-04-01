(ns jiksnu.routes.helpers
  #_(:use [clojurewerkz.route-one.core :only [named-path named-url]]))

(defn named-path
  [& _])

(defn add-route! [& _])

(defn formatted-path
  ([name]
     (str #_(named-path name) ".:format"))
  ([name params]
     (str #_(named-path name params) ".:format"))
   ([name params format]
     (str #_(named-path name params) "." format)))

(defn formatted-url
  ([name]
     (str #_(named-url name) ".:format"))
  ([name params]
     (str #_(named-url name params) ".:format"))
   ([name params format]
     (str #_(named-url name params) "." format)))
