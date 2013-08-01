(ns jiksnu.routes.auth-routes
  (:use [ciste.commands :only [add-command!]]
        [ciste.initializer :only [definitializer]]
        [ciste.loader :only [require-namespaces]]
        [clojurewerkz.route-one.core :only [add-route! named-path]]
        [jiksnu.routes.helpers :only [formatted-path]])
  (:require [jiksnu.actions.auth-actions :as auth]))

(add-route! "/main/login"                         {:named "login page"})
(add-route! "/main/logout"                        {:named "logout page"})
(add-route! "/main/guest-login"                   {:named "guest login page"})
(add-route! "/main/password"                      {:named "password page"})
(add-route! "/model/authenticationMechanisms/:id" {:named "authentication-mechanism model"} )

(defn routes
  []
  [[[:get  "/api/account/verify_credentials.:format"]          #'auth/verify-credentials]
   [[:post (named-path "guest login page")]                    #'auth/guest-login]
   [[:get  (named-path "login page")]                          #'auth/login-page]
   [[:post (named-path "login page")]                          #'auth/login]
   [[:get  (named-path "logout page")]                         #'auth/logout]
   [[:post (named-path "logout page")]                         #'auth/logout]
   [[:get  (named-path "password page")]                       #'auth/password-page]
   [[:get  (formatted-path "authentication-mechanism model")]  #'auth/show]])

(add-command! "auth"   #'auth/login)
(add-command! "whoami" #'auth/whoami)

