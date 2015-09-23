(ns jiksnu.modules.web.routes.request-token-routes
  (:require [jiksnu.actions.request-token-actions :as actions.request-token]))

(defn pages
  []
  [
   [{:name "request-tokens"} {:action #'actions.request-token/index}]
   ]
  )
