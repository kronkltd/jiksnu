(ns jiksnu.model
  (:require [goog.json :as json]
             [goog.string :as gstring]
            [goog.string.format :as gformat]
            [jiksnu.logging :as jl]
            [jiksnu.util.backbone :as backbone]
            [jiksnu.util.ko :as ko]
            [jiksnu.websocket :as ws]
            [lolg :as log])
  (:use-macros [jiksnu.macros :only [defvar]]
               [purnam.core :only [? ?> ! !> f.n def.n do.n this self
                                   obj arr def* do*n def*n f*n]]))

(def *logger* (log/get-logger "jiksnu.model"))

(def
  ^{:doc "The main backbone model"}
  _model)

(def
  ^{:doc "This is the main view model bound to the page"}
  _view)

(def names
  [["activity"                 "activities"
    "Activity"                 "Activities"]
   ["authentication-mechanism" "authenticationMechanisms"
    "AuthenticationMechanism"  "AuthenticationMechanisms"]
   ["client" "clients"
    "Client" "Clients"]
   ["conversation"             "conversations"
    "Conversation"             "Conversations"]
   ["domain"                   "domains"
    "Domain"                   "Domains"]
   ["feed-source"              "feedSources"
    "FeedSource"               "FeedSources"]
   ["feed-subscription"        "feedSubscriptions"
    "FeedSubscription"         "FeedSubscriptions"]
   ["group"                    "groups"
    "Group"                    "Groups"]
   ["resource"                 "resources"
    "Resource"                 "Resources"]
   ["stream"                   "streams"
    "Stream"                   "Streams"]
   ["subscription"             "subscriptions"
    "Subscription"             "Subscriptions"]
   ["user"                     "users"
    "User"                     "Users"]])

(def class-names (map #(nth % 2) names))

(def
  ^{:doc "The list of model names"}
  model-names (map #(nth % 1) names))

(def collection-name
  (->> names
       (map (fn [[k v _]] [k v]))
       (into {})))

;; Observable operations

(defn init-observable
  "Store an observable copy of the model in the model cache"
  [model-name id observable-model model]
  (let [observable (.viewModel js/kb model)]
    (log/finer *logger* (gstring/format "setting observable (already loaded): %s(%s)" model-name id))
    (aset observable-model id observable)
    observable))

(defn fetch-model
  [this]
  (let [model-name (first
                    (first
                     (filter
                      (fn [[_ _ class-name]]
                        (= class-name (.-type this)))
                      names)))]
    ;; TODO: trigger an event to do this
    (ws/send "get-model" (array model-name (.-id this)))))

;; Protocols

(defprotocol Collection2
  (fetch [this])
  )

;; (defprotocol Model)

(defrecord Pages2 [name]
    Collection2

    (fetch [this]
      (fetch-model this)))

(def.n Pages2.prototype.fetch
  []
  (fetch-model this))


;; Model helpers

(defn initializer
  "used for logging initialization of a model"
  [m coll]
  (this-as this
    (let [n (.-type this)]
      (log/finer *logger* (str "Creating record: " n
                               " " (json/serialize m))))))

(defn page-add
  [this id]
  (let [a (.get this "items")]
    (.set this "items" (clj->js (concat [id] a)))))

;; Models

(def Collection
  (.extend
   backbone/Collection
   (obj)))

(def Model
  (.extend
   backbone/Model
   (obj
    :initialize initializer
    :stub "STUB"
    :idAttribute "_id"
    :fetch (fn [] (fetch-model this))
    :url (fn [] (str "/model/"
                    (.-stub this) "/"
                    (.-id this) ".model"))
    :defaults (fn [] (obj :loaded false)))))

;; Page

(def Page
  (.extend
   Model
   (obj
    :type "Page"
    :idAttribute "id"
    :defaults {
               :page         1
               :loaded       false
               :pageSize     0
               :items        (array) #_(backbone/Collection.)
               :recordCount  0
               :totalRecords 0
               }
    )))

(def.n Page.prototype.hasNext
  []
  (< (* (.page this)
        (.pageSize this))
     (.totalRecords this)))

(def.n Page.prototype.popItem
  []
  (let [a (.get this "items")
        i (first a)]
    (.log js/console this)
    (.set this "items" (clj->js (rest a)))
    i))

(def.n Page.prototype.fetch
  []
  (when-not (.-loaded this)
    (! this.loaded true)
    (ws/send "get-page" (arr (.get this "id")))))

(def.n Page.prototype.addItem
  [id]
  (page-add this id))

(def Pages
  (.extend
   Collection
   (obj
    :model Page
    :type "Pages")))

(def.n Pages.prototype.getPage
  [name]
  (.get this name))

(def PageModel
  (.extend
   Model
   (obj
    :defaults (fn []
                 (.extend js/_
                     (obj
                      :_id   nil
                      :pages (Pages.))
                   (.defaults (.-prototype Model)))))))

(defn extend-page-model
  [& args]
  (fn []
    (.extend js/_
        (apply js-obj args)
      (.defaults (.-prototype PageModel)))))

(def pages         (Pages.))

;; Models

(def Notification
  (.extend
   PageModel
   (obj
    :type "Notification"
    ;; :dismiss (fn []
    ;;             (this-as this
    ;;               (.remove (.-collection this) this)))
    :default (extend-page-model
               "message" ""
               "level"   ""))))

(def Domain
  (.extend
   PageModel
   (obj
    :type "Domain"
    :stub "domains"
    :defaults (extend-page-model
                "xmpp"       "unknown"
                "discovered" nil
                "created"    nil
                "updated"    nil
                "links"      (array)))))

(def Resource
  (.extend
   PageModel
   (obj
    :type "Resource"
    :stub "resources"
    :defaults (extend-page-model
                "url"         nil
                "title"       nil
                "domain"      nil
                "status"      nil
                "contentType" nil
                "encoding"    nil
                "location"    nil
                "created"     nil
                "properties"  nil
                "links"       (array)
                "updated"     nil))))

(def User
  (.extend
   PageModel
   (obj
    :type "User"
    :stub "users"
    :defaults (extend-page-model
                "url"          nil
                "avatarUrl"    nil
                "id"           nil
                "uri"          nil
                "bio"          nil
                "username"     nil
                "discovered"  nil
                "location"     ""
                "local"        false
                "domain"       nil
                "updateSource" nil
                "created"      nil
                "updated"      nil
                "links"        (array)
                "displayName" nil
                "name"  nil))))

(def Activity
  (.extend
   PageModel
   (obj
    :type "Activity"
    :stub "activities"
    :defaults
    (extend-page-model
     "_id"           ""
     "author"        ""
     "uri"           ""
     "url"           nil
     "links"         (array)
     "source"        "unknown"
     "comments"      (array)
     "resources"     (array)
     "comment-count" 0
     "created"       nil
     "published"     nil
     "conversation"  nil
     "local"        false
     "title"         nil
     "mentioned"     (array)
     "tags"          (array)
     "geo"           nil
     "object"        (obj :type nil)
     "like-count"    0
     "updateSource" nil
     "enclosures"    (array)))))

(def Group
  (.extend
   PageModel
   (obj
    :type "Group"
    :stub "groups"
    :defaults (extend-page-model
                "from"     nil
                "to"       nil
                "homepage" ""
                "fullname" ""
                "nickname" ""))))

(def Subscription
  (.extend
   PageModel
   (obj
    :type "Subscription"
    :stub "subscriptions"
    :defaults (extend-page-model
                "from"    nil
                "to"      nil
                "created" nil
                "pending" nil
                "local"   nil))))

(def FeedSource
  (.extend
   PageModel
   (obj
    :type "FeedSource"
    :stub "feed-sources"
    :defaults (extend-page-model
                "callback" nil
                "created"  nil
                "updated"  nil
                "status"   nil
                "domain"   nil
                "challenge" nil
                "hub"      nil
                "mode"     nil
                "topic"    nil
                "watchers" (array)
                "title"    "Feed"))))

(def FeedSubscription
  (.extend
   PageModel
   (obj
    :type "FeedSubscription"
    :stub "feed-subscriptions"
    :defaults (extend-page-model
               "domain"   nil
               "callback" nil
               "url"      nil))))

(def Conversation
  (.extend
   PageModel
   (obj
    :type "Conversation"
    :stub "conversations"
    :defaults (extend-page-model
                "uri"           nil
                "url"           nil
                "domain"        nil
                "itemCount"     0
                "update-source" nil
                "lastUpdated"   nil
                "created"       nil
                "activities"    nil
                "updated"       nil))))

(def AuthenticationMechanism
  (.extend
   PageModel
   (obj
    :type        "AuthenticationMechanism"
    :stub        "authenticationMechanisms"
    :defaults (extend-page-model
               "user" nil
               "value" nil))))

(def Stream
  (.extend
   PageModel
   (obj
    :type        "Stream"
    :stub        "streams"
    :defaults (extend-page-model
                "user" nil
                "name" nil))))

(def Client
  (.extend
   PageModel
   (obj
    :type        "Client"
    :stub        "clients"
    :defaults (extend-page-model
                "created" nil
                "updated" nil
                ))))

(defvar Statistics
  [this]
  (doto this
    (ko/assoc-observable "activities")
    (ko/assoc-observable "conversations")
    (ko/assoc-observable "domains")
    (ko/assoc-observable "groups")
    (ko/assoc-observable "feedSources")
    (ko/assoc-observable "feedSubscriptions")
    (ko/assoc-observable "subscriptions")
    (ko/assoc-observable "users")))

(defvar PostForm
  [this]
  (doto this
    (ko/assoc-observable "visible" false)
    (ko/assoc-observable "currentPage" "note")))

;; Collections

(def ^{:doc "collection of activities"}
  Activities
  (.extend backbone/Collection
           (obj
            :type "Activities"
            :urlRoot "/main/notices/"
            :model Activity)))

(def AuthenticationMechanisms
  (.extend backbone/Collection
           (obj
            :type "AuthenticationMechanisms"
            :model AuthenticationMechanism)))

(def Clients
  (.extend backbone/Collection
           (obj
            :type         "Clients"
            :model        Client)))

(def Conversations
  (.extend backbone/Collection
           (obj
            :type         "Conversations"
            :model        Conversation)))

(def Domains
  (.extend backbone/Collection
           (obj
            :type    "Domains"
            :urlRoot "/main/domains/"
            :model   Domain)))

(def FeedSources
  (.extend backbone/Collection
           (obj
            :type "FeedSources"
            :model FeedSource)))

(def FeedSubscriptions
  (.extend backbone/Collection
           (obj
            :type "FeedSubscription"
            :model FeedSubscription)))

(def Groups
  (.extend backbone/Collection
           (obj
            :model Group
            :type "Groups")))

(def Notifications
  (.extend
   backbone/Collection
   (obj
    :model Notification
    :type "Notifications"

    ;; Add a new notification
    :addNotification
    (fn [message]
      (let [notification (Notification.)]
        (.set notification "message" message)
        (.push this notification))))))

(def Resources
  (.extend backbone/Collection
           (obj
            :type    "Resources"
            :model   Resource)))

(def Streams
  (.extend backbone/Collection
           (obj
            :model Stream
            :type "Streams")))

(def Subscriptions
  (.extend backbone/Collection
           (obj
            :model Subscription
            :type "Subscriptions")))

(def Users
  (.extend backbone/Collection
           (obj
            :type         "Users"
            :model        User)))

;; Collection references
;; TODO: remove

(def activities    (Activities.))
(def authentication-mechanisms (AuthenticationMechanisms.))
(def clients (Clients.))
(def conversations  (Conversations.))
(def domains       (Domains.))
(def feed-sources  (FeedSources.))
(def feed-subscriptions (FeedSubscriptions.))
(def groups        (Groups.))
(def notifications (Notifications.))
(def resources     (Resources.))
(def streams       (Streams.))
(def subscriptions (Subscriptions.))
(def users         (Users.))

;; Viewmodel

(def ^{:doc "The main view model for the site"} AppViewModel
  (let [defaults (obj
                  :activities               activities
                  :authenticationMechanisms authentication-mechanisms
                  :conversations            conversations
                  :domains                  domains
                  :clients                  clients
                  :currentUser              nil
                  :feedSources              feed-sources
                  :feedSubscriptions        feed-subscriptions
                  :followers                (array)
                  :following                (array)
                  :formats                  (array)
                  :groups                   groups
                  :loaded                   false
                  :pages                    pages
                  :postForm                 (PostForm.)
                  :notifications            notifications
                  :resources                resources
                  :showComments             false
                  :statistics               nil
                  :streams                  streams
                  :subscriptions            subscriptions
                  :targetActivity           nil
                  :targetConversation       nil
                  :targetDomain             nil
                  :targetFeedSource         nil
                  :targetGroup              nil
                  :targetResource           nil
                  :targetUser               nil
                  :title                    nil
                  :users                    users)]
    ;; (doseq [[model-name collection-name class-name] names]
    ;;   (aset defaults name nil))
    (.extend backbone/Model
             (obj
              :defaults defaults))))

;; model functions

(defn get-model-obj
  [model-name id]
  (if id
    (if-let [coll-name (or (collection-name model-name)
                           model-name)]
      (if-let [coll (.get _model coll-name)]
        (if-let [m (.get coll id)]
          m
          (do
            (log/fine *logger* (gstring/format "Creating model: %s(%s)" model-name id))
            (let [m (.push coll (obj :_id id))]
              (.fetch m)
              m
              )))
        (throw (str "Could not get collection: " coll-name)))
      (throw (str "Could find collection name for: " model-name)))
    (throw "id can not be null")))

(defn get-page-obj
  [page-name]
  (log/fine *logger* (str "getting page: " page-name))
  (if-let [page (.get pages page-name)]
    page
    (do (.add pages (obj :id page-name))
        (let [page (.get pages page-name)]
          (.fetch page)
          page))))

(defn get-sub-page-obj
  "Returns the page for the name from the view's page info.

Returns a viewmodel"
  [model-name id name]
  (log/fine *logger* (gstring/format "getting sub page: %s(%s) => %s" model-name id name))
  (let [m (get-model-obj model-name id)
        coll (.get m "pages")]
    (if-let [page (.get coll name)]
      page
      (do (.add coll (obj :id name))
          (let [m (.get coll name)]
            (ws/send "get-sub-page"
                     (array model-name id name))
            m)))))

;; observable functions

(defn get-model
  "Given a model name and an id, return an observable representing that model"
  [model-name id]
  (if id
    (if (= (type id) js/String)
      (if-let [m (get-model-obj model-name id)]
        (do
          (log/finer *logger* (str "creating viewmodel: " m))
          (.viewModel js/kb m))
        (throw (js/Error. "Could not get model")))
      (throw (js/Error. (str id " is not a string"))))
    (throw (js/Error. "id is undefined"))))

(defn get-page
  "Returns the page for the name from the view's page info.

Returns a viewmodel"
  [name]
  (log/fine *logger* (str "getting page: " name))
  (let [page (get-page-obj name)]
    (.viewModel js/kb page)))

(defn get-sub-page
  "Returns the page for the name from the view's page info.

Returns a viewmodel"
  [model-name id name]
  (log/fine *logger* (gstring/format "getting sub-page: %s(%s) => %s" model-name id name))
  (let [page (get-sub-page-obj model-name id name)]
    (.viewModel js/kb page)))

