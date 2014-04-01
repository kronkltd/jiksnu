(ns jiksnu.routes.dialback-routes
  (:require [ciste.initializer :refer [definitializer]]
            [jiksnu.actions.dialback-actions :as dialback]
            [jiksnu.routes.helpers :refer [add-route! named-path]]))

(add-route! "/api/dialback"              {:named "dialback"})

(defn routes
  []
  [
   [[:post (named-path "dialback")]  #'dialback/confirm]
   ])
