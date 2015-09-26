(ns jiksnu.modules.web.routes.auth-routes
  (:require [cemerick.friend :as friend]
            [clojure.tools.logging :as log]
            [jiksnu.actions.auth-actions :as auth]
            [jiksnu.modules.http.resources :refer [add-group! defresource defgroup]]
            [jiksnu.modules.web.core :refer [jiksnu]]
            [jiksnu.modules.web.helpers :refer [angular-resource page-resource]]
            [liberator.representation :refer [as-response ring-response]]
            [octohipster.mixins :as mixin]))

(defgroup jiksnu auth
  :name "Authentication"
  :description "Authentication routes")

(defresource auth :register
  :url "/main/register"
  :allowed-methods [:get :post]
  :methods {:get {:summary "Register Page"
                  :state "registerPage"}
            :post {:summary "Do Register"
                   :parameters {
                                :username {:in :formData
                                           :type "string"
                                           }
                                }
                   }}
  :mixins [angular-resource]
  :parameters {

               }
  :post! (fn [ctx]
           (actions.user/register (:params (:request ctx))))
  )

(defresource auth :login
  :url "/main/login"
  :mixins [angular-resource]
  :allowed-methods [:get :post]
  :methods {:get  {:summary "Login Page"
                   :state "loginPage"}
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

(defresource auth :logout
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

(defresource auth :verify
  :methods {:get {:summary "Verify Credentials"}}
  :url "/api/account/verify_credentials.json"
  :exists? (fn [ctx]
             {:data (auth/verify-credentials)}))
