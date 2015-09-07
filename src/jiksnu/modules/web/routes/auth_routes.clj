(ns jiksnu.modules.web.routes.auth-routes
  (:require [cemerick.friend :as friend]
            [clojure.tools.logging :as log]
            [jiksnu.actions.auth-actions :as auth]
            [jiksnu.modules.http.resources
             :refer [defresource defgroup]]
            [jiksnu.modules.web.helpers
             :refer [angular-resource page-resource]]
            [liberator.representation :refer [
                                              as-response
                                              ring-response
                                              ]]
            [octohipster.mixins :as mixin]))

(defgroup auth
  :name "Authentication"
  :description "Authentication routes"
  )

(defresource auth register-page
  :url "/main/register"
  :mixins [angular-resource])

(defresource auth login
  :url "/main/login"
  :mixins [angular-resource]
  :allowed-methods [:post]
  :params {
           :username {:in :formData
                      :type "string"
                      :description "The username"
                      :required true}
           :password {:in :formData
                      :type "string"
                      :description "the password"
                      :required true}
           }
  :available-media-types ["application/json"]
  :post! (fn [{:as ctx
              {{:keys [username password]} :params} :request}]
           true)
  :post-redirect? false
  :handle-created (fn [ctx]
                    (ring-response
                     (friend/authenticate-response
                      (:request ctx)
                      {:body "ok"}))))

(defresource auth logout
  :url "/main/logout"
  :allowed-methods [:post]
  :available-media-types ["application/json"]
  :post! (fn [ctx]
           (log/info "logout handler")
           true)
  :handle-created (fn [ctx]
                    (ring-response
                     (friend/logout* (as-response {:data "ok"} ctx)))))

;; (defresource auth verify-credentials
;;   :url "/api/account/verify_credentials.json"
;;   :exists? (fn [ctx]
;;              {:data (auth/verify-credentials)}))

