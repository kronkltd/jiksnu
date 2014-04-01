(ns jiksnu.routes.confirm-routes
  (:require [jiksnu.actions :as actions]
            [jiksnu.routes.helpers :refer [add-route! named-path]]))

(add-route! "/main/confirm"              {:named "confirm"})

(defn routes
  []
  [
   [[:get    (named-path     "confirm")]          #'actions/confirm]
   ])
