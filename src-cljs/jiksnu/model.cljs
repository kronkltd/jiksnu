(ns jiksnu.model
  (:require [goog.json :as json]
            [goog.string :as gstring]
            [goog.string.format :as gformat]
            [jiksnu.logging :as jl]
            [jiksnu.util.backbone :as backbone]
            [jiksnu.util.ko :as ko]
            [jiksnu.websocket :as ws]
            [lolg :as log])
  (:use-macros [jiksnu.macros :only [defcollection defmodel defvar]]
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
    (log/finer *logger* (str "setting observable (already loaded): "
                             model-name "(" id ")"))
    (! observable-model.|id| observable)
    observable))

;; Protocol helpers

(defn get-model-name
  [model]
  (first
   (first
    (filter
     (fn [[_ _ class-name]]
       (= class-name (.-type model)))
     names))))

(defn fetch-model
  [this]
  (let [model-name (get-model-name this)]
    ;; TODO: trigger an event to do this
    (ws/send "get-model" (array model-name (.-id this)))))

(defn page-add
  [this id]
  (let [a (.get this "items")]
    (.set this "items" (clj->js (concat [id] a)))))

;; Model helpers

(defn initializer
  "used for logging initialization of a model"
  [m coll]
  (this-as this
    (let [n (.-type this)]
      (log/finer *logger* (str "Creating record: " n
                               " " (json/serialize m))))))

;; Protocols

(defprotocol Collection2
  ;; (fetch [this])
  )

(defprotocol Model2
  (fetch [this])
  (-fetch2 [this]))

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
    :idAttribute "_id"
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
               :items        (array)
               :recordCount  0
               :totalRecords 0
               }
    )))

(def Pages
  (.extend
   Collection
   (obj
    :model Page
    :type "Pages")))

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

(def pages         (Pages.))

;; Models

(defmodel Activity "activities"
  :_id           ""
  :author        ""
  :uri           ""
  :url           nil
  :links         (array)
  :source        "unknown"
  :comments      (array)
  :resources     (array)
  :comment-count 0
  :created       nil
  :published     nil
  :conversation  nil
  :local        false
  :title         nil
  :mentioned     (array)
  :tags          (array)
  :geo           nil
  :object        (obj :type nil)
  :like-count    0
  :updateSource nil
  :enclosures    (array))

(defmodel  AuthenticationMechanism "authenticationMechanisms"
  :user nil
  :value nil)

(defmodel Client "clients"
  :created nil
  :updated nil)

(defmodel Conversation "conversations"
  :uri           nil
  :url           nil
  :domain        nil
  :itemCount     0
  :update-source nil
  :lastUpdated   nil
  :created       nil
  :activities    nil
  :updated       nil)

(defmodel Domain "domains"
  :xmpp       "unknown"
  :discovered nil
  :created    nil
  :updated    nil
  :links      (array))

(defmodel FeedSource "feed-sources"
  :callback nil
  :created  nil
  :updated  nil
  :status   nil
  :domain   nil
  :challenge nil
  :hub      nil
  :mode     nil
  :topic    nil
  :watchers (array)
  :title    "Feed")

(defmodel FeedSubscription "feed-subscriptions"
  :domain   nil
  :callback nil
  :url      nil)

(defmodel Group "groups"
  :from     nil
  :to       nil
  :homepage ""
  :fullname ""
  :nickname "")

(defmodel Notification "notifications"
  :message ""
  :level   "")

(defmodel Resource "resources"
  :url         nil
  :title       nil
  :domain      nil
  :status      nil
  :contentType nil
  :encoding    nil
  :location    nil
  :created     nil
  :properties  nil
  :links       (array)
  :updated     nil)

(defmodel Stream "streams"
  :user nil
  :name nil)

(defmodel Subscription "subscriptions"
  :from    nil
  :to      nil
  :created nil
  :pending nil
  :local   nil)

(defmodel User "users"
  :id           nil
  :avatarUrl    nil
  :bio          nil
  :created      nil
  :discovered   nil
  :displayName  nil
  :domain       nil
  :links        (array)
  :local        false
  :location     ""
  :name         nil
  :updateSource nil
  :uri          nil
  :url          nil
  :username     nil
  :updated      nil)

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

(defcollection Activities               Activity)
(defcollection AuthenticationMechanisms AuthenticationMechanism)
(defcollection Clients                  Client)
(defcollection Conversations            Conversation)
(defcollection Domains                  Domain)
(defcollection FeedSources              FeedSource)
(defcollection FeedSubscriptions        FeedSubscription)
(defcollection Groups                   Group)
(defcollection Notifications            Notification)
(defcollection Resources                Resource)
(defcollection Streams                  Stream)
(defcollection Subscriptions            Subscription)
(defcollection Users                    User)

;; Object Properties

(! Activities.prototype.urlRoot "/main/notices/")
(! Domain.prototype.urlRoot     "/main/domains/")

;; Object Methods

(def.n Model.prototype.fetch
  []
  (fetch-model this))

(def.n Model.prototype.url
  []
  (str "/model/"
       (.-stub this) "/"
       (.-id this) ".model"))

(def.n Notification.prototype.dismiss
  []
  (.remove (.-collection this) this))

(def.n Notifications.prototype.addNotification
  [message]
  (let [notification (Notification.)]
    (.set notification "message" message)
    (.push this notification)))

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

(def.n Pages.prototype.getPage
  [name]
  (.get this name))

;; Type extensions

(extend-type Model
  Model2

  (fetch [this] (fetch-model this)))

;; Collection references
;; TODO: remove

(def activities                (Activities.))
(def authentication-mechanisms (AuthenticationMechanisms.))
(def clients                   (Clients.))
(def conversations             (Conversations.))
(def domains                   (Domains.))
(def feed-sources              (FeedSources.))
(def feed-subscriptions        (FeedSubscriptions.))
(def groups                    (Groups.))
(def notifications             (Notifications.))
(def resources                 (Resources.))
(def streams                   (Streams.))
(def subscriptions             (Subscriptions.))
(def users                     (Users.))

;; Viewmodel

(def AppViewModel
  (.extend
   backbone/Model
   (obj
    :defaults {
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
               :users                    users
               })))

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

