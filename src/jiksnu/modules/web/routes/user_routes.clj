(ns jiksnu.modules.web.routes.user-routes
  (:require [cemerick.friend :as friend]
            [clojure.data.json :as json]
            [clojure.tools.logging :as log]
            [jiksnu.actions.group-actions :as group]
            [jiksnu.actions.stream-actions :as stream]
            [jiksnu.actions.subscription-actions :as actions.subscription]
            [jiksnu.actions.user-actions :as user]
            [jiksnu.model.user :as model.user]
            [jiksnu.modules.http.resources :refer [defresource defgroup]]
            [jiksnu.modules.web.helpers :refer [angular-resource page-resource]]
            [octohipster.mixins :as mixin])
  (:import jiksnu.model.User))

;; =============================================================================

(defgroup users
  :url "/main/users")

(defresource users collection
  :desc "Collection route for users"
  :mixins [angular-resource])

(defresource users resource
  :url "/{_id}"
  :mixins [angular-resource])

;; =============================================================================

(defgroup users-api
  :url "/api/users")

(defresource users-api collection
  :mixins [page-resource]
  :ns 'jiksnu.actions.user-actions)

(defresource users-api item
  :desc "Resource routes for single User"
  :url "/{_id}"
  :mixins [mixin/item-resource]
  :available-media-types ["application/json"]
  :presenter (partial into {})
  :exists? (fn [ctx]
             (let [id (-> ctx :request :route-params :_id)]
               {:data (model.user/fetch-by-id id)})))

(defresource users-api followers-collection
  :url "/{_id}/followers"
  :mixins [mixin/item-resource]
  :available-media-types ["application/json"]
  :exists? (fn [ctx]
             (let [id (-> ctx :request :route-params :_id)]
               (let [user (model.user/fetch-by-id id)]
                 (let [[_ page] (actions.subscription/get-subscribers user)]
                   {:data (log/spy :info page)})))))

;; =============================================================================

(defn routes
  []
  [
   ;; [[:get    "/api/friendships/exists.:format"] #'user/exists?]

   ;; [[:get    "/api/people/@me/@all"]            #'user/index]

   ;; [[:get    "/api/people/@me/@all/:id"]        #'user/show]
   ;; [[:get    "/main/profile"]                   #'user/profile]
   ;; [[:get    "/main/register"]                  #'user/register-page]
   ;; [[:get    "/main/xrd"]                       #'user/user-meta]
   ;; [[:get    "/model/users/:id"]                #'user/show]
   ;; [[:get    "/users"]                          #'user/index]
   ;; [[:post   "/users/:id/update-hub"]           #'user/update-hub]
   ;; [[:post   "/:username"]                      #'user/update]


   [[:get    "/api/user/:username/"]            {:action #'user/show-basic
                                                 :format :as}]
   [[:get    "/api/user/:username/profile"]     {:action #'user/show
                                                 :format :as}]

   [[:post   "/main/profile"]                   #'user/update-profile]
   [[:post   "/main/register"]                  #'user/register]
   [[:get    "/users.:format"]                  #'user/index]
   [[:get    "/users/:id"]                      #'user/show]
   [[:get    "/users/:id.:format"]              #'user/show]
   [[:get    "/users/:user@:domain.:format"]    #'user/show]
   [[:delete "/users/:id"]                      #'user/delete]
   [[:post   "/users/:id/discover.:format"]     #'user/discover]
   [[:post   "/users/:id/discover"]             #'user/discover]
   [[:post   "/users/:id/update.:format"]       #'user/update]
   [[:post   "/users/:id/update"]               #'user/update]
   [[:post   "/users/:id/streams"]              #'user/add-stream]
   [[:post   "/users/:id/delete"]               #'user/delete]
   ])

(defn pages
  []
  [
   [{:name "users"}         {:action #'user/index}]
   ])

(defn sub-pages
  []
  [
   [{:type User :name "activities"}       {:action #'stream/user-timeline}]
   [{:type User :name "subscriptions"}    {:action #'actions.subscription/get-subscriptions}]
   [{:type User :name "subscribers"}      {:action #'actions.subscription/get-subscribers}]
   [{:type User :name "streams"}          {:action #'stream/fetch-by-user}]
   [{:type User :name "groups"}           {:action #'group/fetch-by-user}]

   ])
