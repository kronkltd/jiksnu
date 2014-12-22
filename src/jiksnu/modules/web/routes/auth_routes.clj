(ns jiksnu.modules.web.routes.auth-routes
  (:require [jiksnu.actions.auth-actions :as auth]))

(defn routes
  []
  [[[:get  "/api/account/verify_credentials.:format"]          #'auth/verify-credentials]
   [[:post "/main/guest-login"]                    #'auth/guest-login]
   ;; [[:get  "/main/login"]                          #'auth/login-page]
   [[:post "/main/login"]                          #'auth/login]
   ;; [[:get  "/main/logout"]                         #'auth/logout]
   [[:post "/main/logout"]                         #'auth/logout]
   ;; [[:get  "/main/password"]                       #'auth/password-page]
   ;; [[:get  "/model/authenticationMechanisms/:id"]  #'auth/show]
   ])


