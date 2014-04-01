(ns jiksnu.routes.setting-routes
  (:require [jiksnu.actions.setting-actions :as setting]
            [jiksnu.routes.helpers :refer [add-route! named-path]]))

(add-route! "/settings/avatar" {:named "avatar settings"})

(defn routes
  []
  [[[:get "/api/statusnet/config.:format"] #'setting/config-output]
   [[:get (named-path "avatar settings")]  #'setting/avatar-page]
   [[:get "/settings/oauthapps"]           #'setting/oauth-apps]])


