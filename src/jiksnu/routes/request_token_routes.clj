(ns jiksnu.routes.request-token-routes
  (:require [jiksnu.actions.request-token-actions :as actions.request-token]))

(defn routes
  []
  []
  )

(defn pages
  []
  [
   {:name "request-tokens"} {:action #'actions.request-token/index}
   ]
  )
