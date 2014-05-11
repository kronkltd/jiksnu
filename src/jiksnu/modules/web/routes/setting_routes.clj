(ns jiksnu.modules.web.routes.setting-routes
  (:require [jiksnu.actions.setting-actions :as setting]))

(defn routes
  []
  [[[:get "/api/statusnet/config.:format"] #'setting/config-output]
   [[:get "/settings/avatar"]              #'setting/avatar-page]
   [[:get "/settings/oauthapps"]           #'setting/oauth-apps]])


