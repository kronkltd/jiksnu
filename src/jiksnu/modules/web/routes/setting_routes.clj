(ns jiksnu.modules.web.routes.setting-routes
  (:require [jiksnu.actions.setting-actions :as setting]
            [jiksnu.modules.http.resources :refer [defresource defgroup]]
            [jiksnu.modules.web.core :refer [jiksnu]]
            [octohipster.mixins :as mixin]))

(defgroup jiksnu statusnet
  :name "Statusnet API"
  :url "/api/statusnet")

(defresource statusnet :config
  :name        "Config"
  :url         "/config.json"
  :handle-ok (fn [ctx] nil)
  :description "Config for interop with statusnet")

(defgroup jiksnu settings-api
  :name "Settings Models"
  :url "/model/settings")

(defresource settings-api :item
  :name "Settings"
  :url ""
  :available-media-types ["application/json"]
  :mixins [mixin/item-resource]
  :exists? (fn [ctx]
             {:data (setting/config-output)}))
