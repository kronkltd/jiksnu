(ns jiksnu.routes.confirm-routes
  (:use [clojurewerkz.route-one.core :only [add-route! named-path]])
  (:require [jiksnu.actions :as actions]))

(add-route! "/main/confirm"              {:named "confirm"})

(defn routes
  []
  [
   [[:get    (named-path     "confirm")]          #'actions/confirm]
   ])
