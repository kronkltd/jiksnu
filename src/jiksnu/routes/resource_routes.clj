(ns jiksnu.routes.resource-routes
  (:use [clojurewerkz.route-one.core :only [add-route! named-path]]
        [jiksnu.actions.resource-actions :only [delete discover index show update]]
        [jiksnu.routes.helpers :only [formatted-path]]))

(add-route! "/resources"              {:named "index resources"})
(add-route! "/resources/:id"          {:named "show resource"})
(add-route! "/resources/:id/delete"   {:named "delete resource"})
(add-route! "/resources/:id/discover" {:named "discover resource"})
(add-route! "/resources/:id/update"   {:named "update resource"})
(add-route! "/model/resources/:id"    {:named "resource model"})

(defn routes
  []
  [
   [[:get    (formatted-path "index resources")]   #'index]
   [[:get    (named-path     "index resources")]   #'index]
   [[:post   (formatted-path "discover resource")] #'discover]
   [[:post   (named-path     "discover resource")] #'discover]
   [[:post   (formatted-path "update resource")]   #'update]
   [[:post   (named-path     "update resource")]   #'update]
   [[:delete (named-path     "show resource")]     #'delete]
   [[:post   (named-path     "delete resource")]   #'delete]
   [[:get    (formatted-path "resource model")]    #'show]
   ])
