(ns jiksnu.routes.dialback-routes
  (:use [ciste.initializer :only [definitializer]]
        [clojurewerkz.route-one.core :only [add-route! named-path]])
  (:require [jiksnu.actions.dialback-actions :as dialback]))

(add-route! "/api/dialback"              {:named "dialback"})

(defn routes
  []
  [
   [[:post (named-path "dialback")]  #'dialback/confirm]
   ])
