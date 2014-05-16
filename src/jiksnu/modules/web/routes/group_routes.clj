(ns jiksnu.modules.web.routes.group-routes
  (:require [jiksnu.actions.conversation-actions :as conversation]
            [jiksnu.actions.group-actions :as group])
  (:import jiksnu.model.Group))

(defn routes
  []
  [
   [[:get  "/api/statusnet/app/memberships/:id.:format"] #'group/user-list]

   [[:get  "/main/groups.:format"]                       #'group/index]
   [[:get  "/main/groups"]                               #'group/index]
   [[:post "/main/groups"]                               #'group/create]
   [[:get  "/main/groups/new"]                           #'group/new-page]
   [[:get  "/main/groups/:name/edit"]                    #'group/edit-page]

   [[:get  "/model/groups/:id.:format"]                  #'group/show]
   [[:get  "/model/groups/:id"]                          #'group/show]

   [[:get  "/users/:id/groups.:format"]                  #'group/fetch-by-user]
   [[:get  "/users/:id/groups"]                          #'group/fetch-by-user]
   ;; [[:get    "/search/group"]                            #'group/search-page]
   ;; [[:post   "/search/group"]                            #'group/search]

   ])

(defn pages
  []
  [
   [{:name "groups"}    {:action #'group/index}]
   ])

(defn sub-pages
  []
  [
   [{:type Group :name "admins"}        {:action #'group/fetch-admins}]
   [{:type Group :name "conversations"} {:action #'conversation/fetch-by-group}]
  ])
