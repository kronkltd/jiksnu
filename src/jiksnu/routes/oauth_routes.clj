(ns jiksnu.routes.oauth-routes
  (:require [jiksnu.actions.oauth-actions :as actions.oauth]))

(defn routes
  []
  [
   [[:get "/oauth/request_token"] #'actions.oauth/request-token]
   [[:get "/oauth/authorize"]     #'actions.oauth/authorize]
   [[:get "/oauth/access_token"]  #'actions.oauth/access-token]
   ]
  )

