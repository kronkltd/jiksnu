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
  :methods {:get {:summary "Register Page"}}
  :mixins [angular-resource])

(defresource auth login
  :url "/main/login"
  :mixins [angular-resource]
  :allowed-methods [:get :post]
  :methods {:get  {:summary "Login Page"}
            :post {:summary "Do Login"
                   :parameters {:username {:in :formData
                                           :type "string"
                                           :description "The username"
                                           :required true}
                                :password {:in :formData
                                           :type "string"
                                           :description "the password"
                                           :required true}}
                   :responses {"200" {:description "Login Response"}}}}
  :available-media-types ["text/html" "application/json"]
  :post! (fn [{:as ctx
              {{:keys [username password]} :params} :request}]
           true)
  :post-redirect? false
  :handle-created (fn [ctx]
                    (friend/authenticate-response
                     (:request ctx)
                     {:body "ok"})))

(defresource auth logout
  :url                   "/main/logout"
  :allowed-methods       [:post]
  :available-media-types ["application/json"]
  :methods {:post {:summary "Do Logout"}}
  :post! (fn [ctx]
           (log/info "logout handler")
           true)
  :handle-created (fn [ctx]
                    (ring-response
                     (friend/logout* (as-response {:data "ok"} ctx)))))

(defresource auth verify-credentials
  :methods {:get {:summary "Verify Credentials"}}
  :url "/api/account/verify_credentials.json"
  :exists? (fn [ctx]
             {:data (auth/verify-credentials)}))

