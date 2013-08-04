(ns jiksnu.routes.favorite-routes
  (:require [jiksnu.actions.favorite-actions :as favorite]))

(defn routes
  []
  [[[:get    "/api/statusnet/app/favorites/:id.:format"]     #'favorite/user-list]])
