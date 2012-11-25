(ns jiksnu.routes.helpers
  (:use [clojurewerkz.route-one.core :only [named-path named-url]]))

(defn formatted-path
  ([name]
     (str (named-path name) ".:format"))
  ([name params]
     (str (named-path name params) ".:format"))
   ([name params format]
     (str (named-path name params) "." format)))

(defn formatted-url
  ([name]
     (str (named-url name) ".:format"))
  ([name params]
     (str (named-url name params) ".:format"))
   ([name params format]
     (str (named-url name params) "." format)))
