(ns jiksnu.routes.client-routes
  (:require [jiksnu.actions.client-actions :as actions.client]))

(defn routes
  []
  []
  )

(defn pages
  []
  [
   [{:name "clients"} {:action #'actions.client/index}]
   ]
  )
