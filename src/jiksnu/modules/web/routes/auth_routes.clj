(ns jiksnu.modules.web.routes.auth-routes
  (:require [clojure.tools.logging :as log]
            [jiksnu.actions.auth-actions :as auth]
            [jiksnu.modules.http.resources
             :refer [defresource defgroup]]
            [jiksnu.modules.web.helpers
             :refer [angular-resource page-resource]]
            [liberator.representation :refer [ring-response]]
            [octohipster.mixins :as mixin]))

(defgroup auth

  )

(defresource auth login
  :url "/main/login"
  :mixins [angular-resource]
  :allowed-methods [:get :post]
  :post! (fn [{:as ctx
              {{:keys [username password]} :params} :request}]
           (auth/login username password)))

(defresource auth logout
  :url "/main/logout"
  :allowed-methods [:post]
  :available-media-types ["application/json"]
  :post! (fn [{:as ctx
              request :request}]
           (log/spy :info request)
           (auth/logout)
           (ring-response {:session {:id nil}
                           :body "OK"
                           :status 200
})
))

(defresource auth verify-credentials
  :url "/api/account/verify_credentials.json"
  :exists? (fn [ctx]
             {:data (auth/verify-credentials)}
             )
  )

(defn routes
  []
  [[[:get  "/api/account/verify_credentials.:format"]          #'auth/verify-credentials]
   [[:post "/main/guest-login"]                    #'auth/guest-login]
   [[:post "/main/login"]                          #'auth/login]
   ;; [[:get  "/main/logout"]                         #'auth/logout]
   [[:post "/main/logout"]                         #'auth/logout]
   ;; [[:get  "/main/password"]                       #'auth/password-page]
   ;; [[:get  "/model/authenticationMechanisms/:id"]  #'auth/show]
   ])


