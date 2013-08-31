(ns jiksnu.routes.client-routes
  (:require [jiksnu.actions.client-actions :as actions.client]))

(defn routes
  []
  [[[:post "/api/client/register"]           {:action #'actions.client/register
                                              :format :json}]]
  )

(defn pages
  []
  [
   [{:name "clients"} {:action #'actions.client/index}]
   ]
  )
