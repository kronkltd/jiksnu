(ns jiksnu.modules.web.routes.home-routes
  (:require [clojure.data.json :as json]
            [taoensso.timbre :as log]
            [jiksnu.actions.activity-actions :as activity]
            [jiksnu.actions.site-actions :as site]
            [jiksnu.modules.web.core :refer [jiksnu]]
            [jiksnu.modules.http.resources :refer [add-group! defresource defgroup resources]]
            [jiksnu.modules.web.helpers :as helpers
             :refer [angular-resource]]
            [octohipster.mixins :as mixin
             :refer [item-resource]]))

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
  :mixins      [item-resource]
  :exists?     (fn [ctx]
                 {:data (site/status (:request ctx))}))

(defresource root :resources
  :name "Resources"
  :url "/resources"
  :mixins [item-resource]
  :available-media-types ["text/html"]
  :exists? (fn [ctx] {:data (str @resources )}))

(defresource root :settings
  :url "/main/settings"
  :name "Settings Page"
  :summary "settings page"
  :mixins [angular-resource]
  :doc {:get {:nickname "settings-page"
              :summary "Settings Page"}})

;; (defresource root register
;;   :name "Register"
;;   :url "/register"
;;   :summary "Register user"
;;   ;; :mixins [item-resource]
;;   :post! (fn [ctx] ctx))
