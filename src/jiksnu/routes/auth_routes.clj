(ns jiksnu.routes.auth-routes
  (:require [jiksnu.actions.auth-actions :as auth]
            [jiksnu.routes.helpers :refer [add-route!]]))

(add-route! "/main/logout"                        {:named "logout page"})
(add-route! "/main/password"                      {:named "password page"})
(add-route! "/model/authenticationMechanisms/:id" {:named "authentication-mechanism model"} )

(defn routes
  []
  [[[:get  "/api/account/verify_credentials.:format"]          #'auth/verify-credentials]
   [[:post "/main/guest-login"]                    #'auth/guest-login]
   [[:get  "/main/login"]                          #'auth/login-page]
   [[:post "/main/login"]                          #'auth/login]
   [[:get  "/main/logout"]                         #'auth/logout]
   [[:post "/main/logout"]                         #'auth/logout]
   [[:get  "/main/password"]                       #'auth/password-page]
   [[:get  "/model/authenticationMechanisms/:id"]  #'auth/show]])


