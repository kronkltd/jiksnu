(ns jiksnu.routes.domain-routes
  (:use [clojurewerkz.route-one.core :only [add-route! named-path]])
  (:require [jiksnu.actions.domain-actions :as domain]))

(add-route! "/.well-known/host-meta"     {:named "host meta"})
(add-route! "/main/domains/:id/discover" {:named "discover domain"})
(add-route! "/main/domains"              {:named "index domains"})
(add-route! "/main/domains/:id"          {:named "show domain"})

(defn routes
  []
  [
   [[:get    (named-path "host meta")]         #'domain/host-meta]
   [[:get    "/.well-known/host-meta.:format"] #'domain/host-meta]
   [[:get    "/main/domains.:format"]          #'domain/index]
   [[:get    (named-path "index domains")]     #'domain/index]
   [[:get    "/main/domains/:id.:format"]      #'domain/show]
   [[:get    (named-path "show domain")]       #'domain/show]
   [[:delete "/main/domains/*"]                #'domain/delete]
   [[:post   (named-path "discover domain")]   #'domain/discover]
   [[:post   "/main/domains/:id/edit"]         #'domain/edit-page]
   [[:post   "/main/domains"]                  #'domain/find-or-create]
   [[:get    "/model/domains/:id.:format"]        #'domain/show]
   ])
