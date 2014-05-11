(ns jiksnu.modules.web.routes.access-token-routes
  (:require [jiksnu.actions.access-token-actions :as actions.access-token]))

(println "loading access token routes")

(defn routes
  []
  [

   [[:post "/oauth/access_token"]      {:action #'actions.access-token/get-access-token
                                        :format :text}]

   ]
  )
