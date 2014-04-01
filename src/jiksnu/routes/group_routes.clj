(ns jiksnu.routes.group-routes
  (:require [jiksnu.actions.group-actions :as group]
            [jiksnu.routes.helpers :refer [add-route! named-path formatted-path]]))

(add-route! "/groups"     {:named "index groups"})
(add-route! "/groups/new" {:named "new group"})
;; (add-route! "/:username/groups" {:named "index user groups"})
(add-route! "/groups/:name/edit" {:named "edit group"})
(add-route! "/model/groups/:id"     {:named "group model"})

(defn routes
  []
  [
   [[:post "/groups"]                                    #'group/create]
   [[:get  "/api/statusnet/app/memberships/:id.:format"] #'group/user-list]
   [[:get  (formatted-path "index groups")]              #'group/index]
   [[:get  (named-path     "index groups")]              #'group/index]
   [[:get  (named-path     "new group")]                 #'group/new-page]
   [[:get  (named-path     "edit group")]                #'group/edit-page]
   ;; [[:get  (formatted-path "index user groups")]         #'group/user-list]
   ;; [[:get  (named-path     "index user groups")]         #'group/user-list]
   ;; [[:get    "/search/group"]                            #'group/search-page]
   ;; [[:post   "/search/group"]                            #'group/search]
   [[:get    (formatted-path "group model")]          #'group/show]

   ])

(defn pages
  []
  [
   [{:name "groups"}    {:action #'group/index}]
   ])

