(ns jiksnu.routes.setting-routes
  (:use [ciste.initializer :only [definitializer]]
        [ciste.loader :only [require-namespaces]]
        [clojurewerkz.route-one.core :only [add-route! named-path]]
        [jiksnu.routes.helpers :only [formatted-path]])
  (:require [jiksnu.actions.setting-actions :as setting]))

(add-route! "/settings/avatar" {:named "avatar settings"})

(defn routes
  []
  [[[:get "/api/statusnet/config.:format"] #'setting/config-output]
   [[:get (named-path "avatar settings")]  #'setting/avatar-page]
   [[:get "/settings/oauthapps"]           #'setting/oauth-apps]])


