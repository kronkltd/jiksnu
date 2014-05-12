(ns jiksnu.modules.web.routes.group-routes
  (:require [jiksnu.actions.group-actions :as group]))

(defn routes
  []
  [
   [[:post "/main/groups"]                               #'group/create]
   [[:get  "/api/statusnet/app/memberships/:id.:format"] #'group/user-list]
   [[:get  "/main/groups.:format"]                       #'group/index]
   [[:get  "/main/groups"]                               #'group/index]
   [[:get  "/main/groups/new"]                           #'group/new-page]
   [[:get  "/main/groups/:name/edit"]                    #'group/edit-page]
   ;; [[:get  "/:username/groups.:format"]                  #'group/user-list]
   ;; [[:get  "/:username/groups"]                          #'group/user-list]
   ;; [[:get    "/search/group"]                            #'group/search-page]
   ;; [[:post   "/search/group"]                            #'group/search]
   [[:get    "/model/groups/:id.:format"]               #'group/show]
   [[:get    "/model/groups/:id"]                       #'group/show]

   ])

(defn pages
  []
  [
   [{:name "groups"}    {:action #'group/index}]
   ])

(defn sub-pages
  []
  [
   [{:type Group :name "admins"}       {:action #'group/fetch-admins}]

   ])
