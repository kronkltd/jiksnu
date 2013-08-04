(ns jiksnu.routes.setting-routes
  (:use [clojurewerkz.route-one.core :only [add-route! named-path]])
  (:require [jiksnu.actions.setting-actions :as setting]))

(add-route! "/settings/avatar" {:named "avatar settings"})

(defn routes
  []
  [[[:get "/api/statusnet/config.:format"] #'setting/config-output]
   [[:get (named-path "avatar settings")]  #'setting/avatar-page]
   [[:get "/settings/oauthapps"]           #'setting/oauth-apps]])


