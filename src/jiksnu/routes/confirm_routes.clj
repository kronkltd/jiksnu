(ns jiksnu.routes.confirm-routes
  (:use [clojurewerkz.route-one.core :only [add-route! named-path]]
        [jiksnu.routes.helpers :only [formatted-path]])
  (:require [jiksnu.actions :as actions]))

(add-route! "/main/confirm"              {:named "confirm"})

(defn routes
  []
  [
   [[:get    (named-path     "confirm")]          #'actions/confirm]
   ])
