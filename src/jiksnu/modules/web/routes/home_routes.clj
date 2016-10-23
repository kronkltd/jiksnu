(ns jiksnu.modules.web.routes.home-routes
  (:require [cemerick.friend :as friend]
            [ciste.core :refer [with-context]]
            [ciste.sections.default :refer [show-section]]
            [jiksnu.actions.site-actions :as site]
            [jiksnu.model.user :as model.user]
            [jiksnu.modules.web.core :refer [jiksnu]]
            [jiksnu.modules.http.resources :refer [add-group! defresource defgroup resources]]
            [jiksnu.modules.web.helpers :refer [angular-resource as-collection-resource]]
            [octohipster.mixins :as mixin]
            [taoensso.timbre :as timbre]))

(defgroup jiksnu root
  :name "Root"
  :url ""
  :summary "Root")

(defresource root :home
  :name        "Home"
  :url         "/"
  :summary     "home page"
  :description "The base page. Shows new public activities"
  :mixins      [angular-resource]
  :doc         {:get {:nickname "home-page"
                      :summary "Home Page"}})

(defresource root :status
  :name        "Status"
  :url         "/status"
  :summary     "Site Status"
  :description "Contains base data used to initialize the front-end application"
  :mixins      [mixin/item-resource]
  :exists?     (fn [ctx]
                 (timbre/info "getting status")
                 {:data (site/status (:request ctx))}))

(defresource root :resources
  :name "Resources"
  :url "/resources"
  :mixins [mixin/item-resource]
  :available-media-types ["text/html"]
  :exists? (fn [ctx] {:data (str @resources)}))

(defresource root :rsd
  :name "Really Simple Discovery"
  :url "/rsd.xml"
  :mixins [mixin/item-resource]
  :available-media-types ["application/xml"]
  :exists? (fn [ctx] {:data (site/rsd)}))

(defresource root :settings
  :url "/main/settings"
  :name "Settings Page"
  :summary "settings page"
  :mixins [angular-resource]
  :doc {:get {:nickname "settings-page"
              :summary "Settings Page"}})

;; (defresource root :oembed
;;   :name "OEmbed"
;;   :url "/main/oembed")

;; =============================================================================

(defgroup jiksnu api
  :name "Pump.io API"
  :url "/api"
  :summary "Pump")

(defresource api :whoami
  :name "Whoami"
  :url "/whoami"
  :mixins [as-collection-resource]
  :exists? (fn [ctx]
             (let [user (model.user/get-user (:current (friend/identity (:request ctx))))]
               {:data (with-context [:http :as] (show-section user))})))
