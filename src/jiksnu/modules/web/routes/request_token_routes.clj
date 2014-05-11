(ns jiksnu.modules.web.routes.request-token-routes
  (:require [jiksnu.actions.request-token-actions :as actions.request-token]))

(defn routes
  []
  [

   [[:post "/oauth/request_token"]      {:action #'actions.request-token/get-request-token
                                         :format :text}]

   [[:post "/oauth/authorize"]          {:action #'actions.request-token/authorize
                                         :format :html
                                         }]

   [[:get "/oauth/authorize"]           {:action #'actions.request-token/show-authorization-form
                                         :format :html}]

   ]
  )

(defn pages
  []
  [
   [{:name "request-tokens"} {:action #'actions.request-token/index}]
   ]
  )
