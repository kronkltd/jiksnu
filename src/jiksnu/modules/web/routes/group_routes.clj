(ns jiksnu.modules.web.routes.group-routes
  (:require [clojure.data.json :as json]
            [clojure.tools.logging :as log]
            [jiksnu.actions.conversation-actions :as conversation]
            [jiksnu.actions.group-actions :as group]
            [jiksnu.model.group :as model.group]
            [jiksnu.modules.http.resources :refer [defresource defgroup]]
            [jiksnu.modules.web.helpers :refer [angular-resource page-resource]]
            [jiksnu.modules.web.routes :as r]
            [octohipster.mixins :as mixin])
  (:import jiksnu.model.Group))

;; =============================================================================

(defgroup groups
  :url "/main/groups"
  :name "groups")

(defresource groups collection
  :mixins [angular-resource])

(defresource groups resource
  :url "/{_id}"
  :mixins [angular-resource])

;; (defresource groups resource

;;   )

;; =============================================================================

(defgroup groups-api
  :url "/model/groups")

(defresource groups-api collection
  :mixins [page-resource]
  :available-formats [:json]
  :ns 'jiksnu.actions.group-actions)

(defresource groups-api item
  :desc "Resource routes for single Group"
  :url "/{_id}"
  :mixins [mixin/item-resource]
  :available-media-types ["application/json"]
  :presenter (partial into {})
  :exists? (fn [ctx]
             (let [id (-> ctx :request :route-params :_id)]
               {:data (model.group/fetch-by-id id)})))

;; =============================================================================

(defn routes
  []
  [
   [[:get  "/api/statusnet/app/memberships/:id.:format"] #'group/user-list]

   [[:get  "/main/groups.:format"]                       #'group/index]
   ;; [[:get  "/main/groups"]                               #'group/index]
   [[:post "/main/groups"]                               #'group/create]
   [[:get  "/main/groups/new"]                           #'group/new-page]
   [[:get  "/main/groups/:name.:format"]                 #'group/show]
   ;; [[:get  "/main/groups/:name"]                         #'group/show]
   ;; [[:get  "/main/groups/:name/edit"]                    #'group/edit-page]
   [[:post "/main/groups/:name/join"]                    #'group/join]

   [[:get  "/model/groups/:id.:format"]                  #'group/show]
   ;; [[:get  "/model/groups/:id"]                          #'group/show]

   [[:get  "/users/:id/groups.:format"]                  #'group/fetch-by-user]
   ;; [[:get  "/users/:id/groups"]                          #'group/fetch-by-user]
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
