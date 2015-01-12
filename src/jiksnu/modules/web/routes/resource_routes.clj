(ns jiksnu.modules.web.routes.resource-routes
  (:require [ciste.loader :refer [require-namespaces]]
            [jiksnu.actions.resource-actions :refer [delete discover index show update]]))

(defn routes
  []
  [
   [[:get    "/resources.:format"]   #'index]
   ;; [[:get    "/resources"]   #'index]
   [[:get    "/resources/:id.:format"]     #'show]
   ;; [[:get    "/resources/:id"]     #'show]
   [[:post   "/resources/:id/discover.:format"] #'discover]
   [[:post   "/resources/:id/discover"] #'discover]
   [[:post   "/resources/:id/update.:format"]   #'update]
   [[:post   "/resources/:id/update"]   #'update]
   [[:delete "/resources/:id"]     #'delete]
   [[:post   "/resources/:id/delete"]   #'delete]
   [[:get    "/model/resources/:id"]    #'show]
   ])

(defn pages
  []
  [
   [{:name "resources"}    {:action #'index}]
   ])
