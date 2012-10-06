(ns jiksnu.routes.auth-routes
  (:use [clojurewerkz.route-one.core :only [add-route! named-path]]
        [jiksnu.routes.helpers :only [formatted-path]])
  (:require [jiksnu.actions.auth-actions :as auth]))

(add-route! "/main/login" {:named "login page"})
(add-route! "/main/password" {:named "password page"})
(add-route! "/model/authenticationMechanisms/:id" {:named "authentication-mechanism model"} )

(defn routes
  []
  [[[:get  "/api/account/verify_credentials.:format"]          #'auth/verify-credentials]
   [[:post "/main/guest-login"]                                #'auth/guest-login]
   [[:get  (named-path "login page")]                          #'auth/login-page]
   [[:post (named-path "login page")]                          #'auth/login]
   [[:get  "/main/logout"]                                     #'auth/logout]
   [[:post "/main/logout"]                                     #'auth/logout]
   [[:get  (named-path "password page")]                       #'auth/password-page]
   [[:get  (formatted-path "authentication-mechanism model")]  #'auth/show]])
