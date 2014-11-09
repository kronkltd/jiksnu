(ns jiksnu.modules.web.routes.core-routes
  (:require [ciste.core :refer [defaction]]
            [jiksnu.modules.web.actions.core-actions :as actions.web.core]))

(defn routes
  []
  [
   [[:get "/nav.js"] {:action #'actions.web.core/nav-info :format :json}]
   ])
