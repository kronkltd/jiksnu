(ns jiksnu.modules.web.routes.favorite-routes
  (:require [jiksnu.actions.favorite-actions :as favorite]))

(defn routes
  []
  [
   [[:get    "/api/statusnet/app/favorites/:id.:format"]     #'favorite/user-list]
   [[:get "/api/user/:username/favorites"]  {:action #'favorite/user-list
                                             :format :as
                                             }]
   ])
