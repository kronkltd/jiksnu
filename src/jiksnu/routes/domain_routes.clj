(ns jiksnu.routes.domain-routes
  (:use [clojurewerkz.route-one.core :only [add-route! named-path]]
        [jiksnu.routes.helpers :only [formatted-path]])
  (:require [jiksnu.actions.domain-actions :as domain]))

(add-route! "/.well-known/host-meta"     {:named "host meta"})
(add-route! "/main/domains/:id/discover" {:named "discover domain"})
(add-route! "/main/domains"              {:named "index domains"})
(add-route! "/main/domains/:id"          {:named "show domain"})
(add-route! "/model/domains/:id"         {:named "domain model"})

(defn routes
  []
  [
   [[:get    (named-path     "host meta")]       #'domain/host-meta]
   [[:get    (formatted-path "host meta")]       #'domain/host-meta]
   [[:get    (formatted-path "index domains")]   #'domain/index]
   [[:get    (named-path     "index domains")]   #'domain/index]
   [[:get    (formatted-path "show domain")]     #'domain/show]
   [[:get    (named-path     "show domain")]     #'domain/show]
   [[:delete "/main/domains/*"]                  #'domain/delete]
   [[:post   (named-path     "discover domain")] #'domain/discover]
   [[:post   "/main/domains/:id/edit"]           #'domain/edit-page]
   [[:post   "/main/domains"]                    #'domain/find-or-create]
   [[:get    (formatted-path "domain model")]    #'domain/show]
   ])
