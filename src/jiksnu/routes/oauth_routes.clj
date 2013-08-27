(ns jiksnu.routes.oauth-routes
  (:use [ciste.commands :only [add-command!]]
        [clojurewerkz.route-one.core :only [add-route! named-path]]
        [jiksnu.routes.helpers :only [formatted-path]])
  (:require [jiksnu.actions.oauth-actions :as actions.oauth]))

(defn routes
  []
  [
   [[:get "/oauth/request_token"] #'actions.oauth/request-token]
   [[:get "/oauth/authorize"]     #'actions.oauth/authorize]
   [[:get "/oauth/access_token"]  #'actions.oauth/access-token]
   ]
  )

