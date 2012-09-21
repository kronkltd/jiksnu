(ns jiksnu.routes.helpers
  (:use [clojurewerkz.route-one.core :only [add-route! named-path]]))

;; TODO: This should allow the format to be filled
(defn formatted-path
  [name]
  (str (named-path name) ".:format"))
