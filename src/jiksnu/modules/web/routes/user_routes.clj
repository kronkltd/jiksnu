(ns jiksnu.modules.web.routes.user-routes
  (:require [cemerick.friend :as friend]
            [ciste.core :refer [with-context]]
            [ciste.sections.default :refer [index-section show-section show-section-minimal]]
            [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.actions.subscription-actions :as actions.subscription]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.subscription :as model.subscription]
            [jiksnu.model.user :as model.user]
            [jiksnu.modules.as.sections.user-sections :refer [format-collection]]
            jiksnu.modules.core.views.stream-views
            [jiksnu.modules.http.resources :refer [defresource defgroup]]
            [jiksnu.modules.web.core :refer [jiksnu]]
            [jiksnu.modules.web.helpers :refer [angular-resource as-collection-resource
                                                defparameter get-user page-resource path
                                                subpage-resource]]
            [jiksnu.util :as util]
            [octohipster.mixins :as mixin]
            [taoensso.timbre :as timbre]))

(defparameter :model.user/id
  :description "The account Id of a user"
  :type "string")

(defparameter :model.user/username
  :description "The user's local username"
  :type "string")

;; =============================================================================

(defgroup jiksnu users
  :url "/main/users"
  :name "Users"
  :description "Routes related to users")

(defresource users :collection
  :name "list users"
  :desc "Collection route for users"
  :mixins [angular-resource]
  :methods {:get {:state "indexUsers"}})

(defresource users :resource
  :url "/{_id}"
  :name "show user"
  :description "show a user"
  :parameters {:_id  (path :model.user/id)}
  :mixins [angular-resource]
  :methods {:get {:state "showUser"}})

;; =============================================================================

(defgroup jiksnu user-pump-api
  :url "/api/user"
  :name "Pump API"
  :description "User api matching pump.io spec")

(defresource user-pump-api :user-info
  :url "/{username}"
  :name "user info"
  :mixins [mixin/item-resource]
  :available-media-types ["application/json"]
  :parameters {:username (path :model.user/username)}
  :exists? (fn [ctx]
             (let [username (get-in ctx [:request :route-params :username])
                   user (get-user ctx)]
               {:data
                {:updated  (:updated user)
                 :published (:created user)
                 :nickname username
                 :profile  (with-context [:http :as]
                             (show-section user))}})))

(defresource user-pump-api :favorites
  :url "/{username}/favorites"
  :name "user favorites"
  :mixins [as-collection-resource]
  :collection-type "activity"
  :indexer (fn [ctx user] (actions.activity/index {}))
  :fetcher (fn [id] (model.activity/fetch-by-id id)))

(defresource user-pump-api :feed
  :url "/{username}/feed"
  :name "user feed"
  :mixins [as-collection-resource]
  :collection-type "activity"
  :indexer (fn [ctx user] (actions.activity/fetch-by-user user))
  :fetcher (fn [id] (model.activity/fetch-by-id id))
  :post! (fn [ctx]
           (timbre/info "receiving post")
           (let [request (:request ctx)]
             (if-let [author (model.user/get-user (:current (friend/identity request)))]
               (let [params (-> (:params (:request ctx))
                                (assoc :author (:_id author)))]
                 (actions.activity/post params))))))

(defresource user-pump-api :feed-major
  :url "/{username}/feed/major"
  :name "user feed major"
  :mixins [as-collection-resource]
  :collection-type "activity"
  :indexer (fn [ctx user] (actions.activity/index {}))
  :fetcher (fn [id] (model.activity/fetch-by-id id)))

(defresource user-pump-api :feed-minor
  :url "/{username}/feed/minor"
  :name "user feed minor"
  :mixins [as-collection-resource]
  :collection-type "activity"
  :indexer (fn [ctx user] (actions.activity/index {}))
  :fetcher (fn [id] (model.activity/fetch-by-id id)))

(defresource user-pump-api :followers
  :url "/{username}/followers"
  :name "user followers"
  :mixins [as-collection-resource]
  :collection-type "person"
  :indexer (fn [ctx user] (nth (actions.subscription/get-subscribers user) 1))
  :fetcher (fn [id] (some-> id model.subscription/fetch-by-id
                            :from model.user/fetch-by-id)))

(defresource user-pump-api :following
  :url "/{username}/following"
  :name "user following"
  :mixins [as-collection-resource]
  :collection-type "person"
  :indexer (fn [ctx user] (nth (actions.subscription/get-subscriptions user) 1))
  :fetcher (fn [id] (some-> id model.subscription/fetch-by-id
                            :to model.user/fetch-by-id)))

(defresource user-pump-api :inbox-direct-major
  :url "/{username}/inbox/direct/major"
  :name "user inbox directmajor"
  :mixins [as-collection-resource]
  :collection-type "activity"
  :indexer (fn [ctx user] (actions.activity/index {}))
  :fetcher (fn [id] (model.activity/fetch-by-id id)))

(defresource user-pump-api :inbox-direct-minor
  :url "/{username}/inbox/direct/minor"
  :name "user inbox direct minor"
  :mixins [as-collection-resource]
  :collection-type "activity"
  :indexer (fn [ctx user] (actions.activity/index {}))
  :fetcher (fn [id] (model.activity/fetch-by-id id)))

(defresource user-pump-api :inbox
  :url "/{username}/inbox"
  :name "user inbox"
  :mixins [as-collection-resource]
  :collection-type "activity"
  :indexer (fn [ctx user] (actions.activity/index {}))
  :fetcher (fn [id] (model.activity/fetch-by-id id)))

(defresource user-pump-api :inbox-major
  :url "/{username}/inbox/major"
  :name "user inbox major"
  :mixins [as-collection-resource]
  :collection-type "activity"
  :indexer (fn [ctx user] (actions.activity/index {}))
  :fetcher (fn [id] (model.activity/fetch-by-id id)))

(defresource user-pump-api :inbox-minor
  :url "/{username}/inbox/minor"
  :name "user inbox minor"
  :mixins [as-collection-resource]
  :collection-type "activity"
  :indexer (fn [ctx user] (actions.activity/index {}))
  :fetcher (fn [id] (model.activity/fetch-by-id id)))

(defresource user-pump-api :lists
  :url "/{username}/lists/person"
  :name "user lists"
  :mixins [as-collection-resource]
  :collection-type "activity"
  :indexer (fn [ctx user] (actions.activity/index {}))
  :fetcher (fn [id] (model.activity/fetch-by-id id)))

(defresource user-pump-api :profile
  :url "/{username}/profile"
  :name "user profile"
  :mixins [as-collection-resource]
  :exists? (fn [ctx]
             (let [user (get-user ctx)]
               {:data (with-context [:http :as] (show-section user))})))

(def outbox-pattern "https://%s/api/user/%s/outbox")

(defn present-outbox
  [rsp]
  (let [page (:body rsp)
        user (:user rsp)
        username (:username user)
        domain (:domain user)
        display-name (str "Activities for " username)
        outbox-url (format outbox-pattern domain username)
        links {:first {:href outbox-url}
               :self {:href outbox-url}
               ;; TODO: add a since link
               :prev {:href outbox-url}}]
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
      (present-outbox rsp))))

;; =============================================================================

(defgroup jiksnu users-api
  :name "User Models"
  :url "/model/users")

(defresource users-api :collection
  :mixins [page-resource]
  :available-formats [:json]
  :ns 'jiksnu.actions.user-actions)

(defresource users-api :item
  :url "/{_id}"
  :name "user routes"
  :description "Resource routes for single User"
  :mixins [mixin/item-resource]
  :parameters {:_id (path :model.user/id)}
  :available-media-types ["application/json"]
  :presenter (partial into {})
  :exists? (fn [ctx]
             (let [id (-> ctx :request :route-params :_id)]
               (when-let [user (model.user/fetch-by-id id)]
                 {:data user}))))

(defresource users-api :activities
  :url "/{_id}/activities"
  :name "user activities"
  :description "Activities of {{username}}"
  :mixins [subpage-resource]
  :target-model "user"
  :subpage "activities"
  :parameters {:_id (path :model.user/id)})

(defresource users-api :groups
  :url "/{_id}/groups"
  :name "user groups"
  :description "Groups of {{username}}"
  :mixins [subpage-resource]
  :target-model "user"
  :subpage "groups"
  :parameters  {:_id (path :model.user/id)})

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
  :name "User Streams"
  :description "Streams of {{username}}"
  :mixins [subpage-resource]
  :target-model "user"
  :subpage "streams"
  :parameters {:_id (path :model.user/id)})
