(ns jiksnu.modules.web.routes.user-routes
  (:require [cemerick.friend :as friend]
            [ciste.config :refer [config]]
            [ciste.core :refer [with-context]]
            [ciste.sections.default :refer [index-section show-section]]
            [clojure.data.json :as json]
            [clojure.tools.logging :as log]
            [jiksnu.actions.group-actions :as group]
            [jiksnu.actions.stream-actions :as stream]
            [jiksnu.actions.subscription-actions :as actions.subscription]
            [jiksnu.actions.user-actions :as user]
            [jiksnu.model.user :as model.user]
            jiksnu.modules.core.filters.stream-filters
            jiksnu.modules.core.views.stream-views
            [jiksnu.modules.http.resources :refer [defresource defgroup]]
            [jiksnu.modules.web.helpers :refer [angular-resource defparameter page-resource
                                                path subpage-resource]]
            [liberator.core :as lib]
            [octohipster.mixins :as mixin])
  (:import jiksnu.model.Activity
           jiksnu.model.Group
           jiksnu.model.User))

(defparameter :model.user/id
  :description "The account Id of a user"
  :type "string")

(defparameter :model.user/username
  :description "The user's local username"
  :type "string")

(defn get-user
  "Gets the user from the context"
  [ctx]
  (let [username (-> ctx :request :route-params :username)]
    (model.user/get-user username)))

;; =============================================================================

(defgroup users
  :url "/main/users"
  :name "Users"
  :description "Routes related to users")

(defresource users :collection
  :name "list users"
  :desc "Collection route for users"
  :mixins [angular-resource])

(defresource users :resource
  :url "/{_id}"
  :name "show user"
  :description "show a user"
  :parameters {:_id  (path :model.user/id)}
  :mixins [angular-resource])

;; =============================================================================

(defgroup user-pump-api
  :url "/api/user"
  :name "Pump API"
  :description "User api matching pump.io spec")


(defresource user-pump-api :profile
  :url "/{username}/profile"
  :name "user profile"
  :mixins [mixin/item-resource]
  :available-media-types ["application/json"]
  :parameters {:username (path :model.user/username)}
  :exists? (fn [ctx]
             (let [user (get-user ctx)]
               {:data user}))
  :presenter (fn [user]
               (with-context [:http :as]
                 (show-section user))))

(def outbox-pattern "https://%s/api/user/%s/outbox")

(defn present-outbox
  [rsp]
  (let [page (:body rsp)
        user (:user rsp)
        username (:username user)
        domain (:domain user)
        display-name (str "Activities for " username)
        outbox-url (format outbox-pattern domain username)
        links {
               :first {:href outbox-url}
               :self {:href outbox-url}
               :prev {
                      ;; TODO: add a since link
                      :href outbox-url
                      }}]
    (-> (index-section (:items page) page)
        (assoc :displayName display-name)
        (assoc :links links)
        (assoc :url outbox-url))))

(defresource user-pump-api :outbox
  :url "/{username}/outbox"
  :mixins [subpage-resource]
  :name "user outbox"
  :parameters {:username (path :model.user/username)}
  :subpage "outbox"
  ;; :target-model "User"
  :target get-user
  :description "Activities by {{username}}"
  :available-formats [:json]
  :presenter
  (fn [rsp]
    (with-context [:http :as]
      (present-outbox rsp)
      )))




;; =============================================================================

(defgroup users-api
  :name "Users API"
  :url "/model/users")

(defresource users-api :collection
  :mixins [page-resource]
  :available-formats [:json]
  :ns 'jiksnu.actions.user-actions)

(defresource users-api :item
  :desc "Resource routes for single User"
  :url "/{_id}"
  :name "user routes"
  :mixins [mixin/item-resource]
  :parameters {:_id (path :model.user/id)}
  :available-media-types ["application/json"]
  :presenter (partial into {})
  :exists? (fn [ctx]
             (let [id (-> ctx :request :route-params :_id)]
               (when-let [user (model.user/fetch-by-id id)]
                 {:data user}))))

(defresource users-api :groups
  :url "/{_id}/groups"
  :name "user groups"
  :mixins [subpage-resource]
  :parameters  {:_id  (path :model.user/id)}
  :subpage "groups"
  :target-model "user"
  :description "Groups of {{username}}"
  :available-formats [:json]
  :presenter (fn [rsp]
               (with-context [:http :json]
                 (let [page (:body rsp)
                       items (:items page)]
                   (-> (if (seq items)
                         (-> (index-section items page))
                         {})
                       (assoc :displayName "Groups"))))))

(defresource users-api :followers
  :url "/{_id}/followers"
  :mixins [mixin/item-resource]
  :parameters {:_id  (path :model.user/id)}
  :available-media-types ["application/json"]
  :exists? (fn [ctx]
             (let [id (-> ctx :request :route-params :_id)]
               (let [user (model.user/fetch-by-id id)]
                 (let [[_ page] (actions.subscription/get-subscribers user)]
                   {:data page})))))

(defresource users-api :following
  :url "/{_id}/following"
  :parameters {:_id  (path :model.user/id)}
  :mixins [mixin/item-resource]
  :available-media-types ["application/json"]
  :exists? (fn [ctx]
             (let [id (-> ctx :request :route-params :_id)]
               (let [user (model.user/fetch-by-id id)]
                 (let [[_ page] (actions.subscription/get-subscriptions user)]
                   {:data page})))))

(defresource users-api :streams
  :url "/{_id}/streams"
  :mixins [subpage-resource]
  :parameters {:_id (path :model.user/id)}
  :subpage "streams"
  :target-model "user"
  :description "Streams of {{username}}"
  :available-formats [:json]
  :presenter (fn [rsp]
               (with-context [:http :json]
                 (let [page (:body (log/spy :info rsp))
                       items (:items page)]
                   #_(-> (log/spy :info (if (seq items)
                          (-> (index-section items page))
                          {}))
                       (assoc :displayName "Streams"))
                   page
                   ))))

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
   ;; [[:post   "/:username"]                      #'user/update-record]


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
   [[:post   "/users/:id/update.:format"]       #'user/update-record]
   [[:post   "/users/:id/update"]               #'user/update-record]
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
   [{:type User :name "outbox"}           {:action #'stream/outbox}]

   ])
