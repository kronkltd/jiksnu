(ns jiksnu.routes.request-token-routes
  (:require [jiksnu.actions.request-token-actions :as actions.request-token]))

(defn routes
  []
  [
   [[:post "/oauth/request_token"]      {:action #'actions.request-token/get-request-token
                                         :format :text}]
   ]
  )

(defn pages
  []
  [
   [{:name "request-tokens"} {:action #'actions.request-token/index}]
   ]
  )
