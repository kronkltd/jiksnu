(ns jiksnu.routes.dialback-routes
  (:require [jiksnu.actions.dialback-actions :as dialback]))

(defn routes
  []
  [
   [[:post "/api/dialback"]  #'dialback/confirm]
   ])
