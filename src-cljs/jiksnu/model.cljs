(ns jiksnu.model
  (:require [jiksnu.backbone :as backbone]
            [jiksnu.ko :as ko]
            [jiksnu.logging :as jl]
            [jiksnu.websocket :as ws]
            [lolg :as log])
  (:use-macros [jiksnu.macros :only [defvar]]))

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

;; Filter predicates

(defn by-name
  [name]
  (fn [x]
    (when (= (.id x) name)
      x)))

;; Observable operations

(defn init-observable
  "Store an observable copy of the model in the model cache"
  [model-name id observable-model model]
  (let [observable (.viewModel js/kb model)]
    (log/finer *logger* (format "setting observable (already loaded): %s(%s)" model-name id))
    (aset observable-model id observable)
    observable))

;; Model Operations

(defn set-model
  [model-name id data]
  (if-let [coll-name (collection-name model-name)]
    (if-let [coll (.get _model coll-name)]
      (if-let [m (.get coll id)]
        (do
          (.set m data)
          (.set m "loaded" true)
          (if-let [om (aget ko/observables coll-name)]
            (init-observable model-name id om m)
            (log/warning (str "Could not find observable model for: " coll-name))))
        (do
          (.add coll data)
          (.get coll id)))
      (log/fine (str "no collection named: " coll-name)))
    (log/warning (str "Could find collection for: " model-name))))

(defn load-model
  "Load the model from the server"
  [model-name id om]
  (log/finest *logger* (format "not loaded: %s(%s)" model-name id))
  (let [coll (.get _model model-name)]
    ;; Create an empty model
    (.add coll (js-obj "_id" id))
    (let [m (.get coll id)]
      ;; Get it from the server
      (.fetch m)
      (let [o (.viewModel js/kb m)]
        ;; cache it for the object model
        (aset om id o)
        o))))

(defn get-observable
  [model-name]
  (aget ko/observables model-name))

(defn- get-model*
  "Inintialize a new model reference based on the params when a cached ref is not found"
  [model-name id]
  (log/finest *logger* (format "observable not found: %s(%s)" model-name id))
  (if-let [coll (.get _model model-name)]
    (let [om (get-observable model-name)]
      (if-let [m (.get coll id)]
        (init-observable model-name id om m)
        (load-model model-name id om)))
    (log/severe *logger* "could not get collection")))

(defn get-model
  "Given a model name and an id, return an observable representing that model"
  [model-name id]
  (if id
    (if (= (type id) js/String)
      (let [om (get-observable model-name)]
        (if-let [o (aget om id)]
          (do
            (log/finest *logger* (format "cached observable found: %s(%s)" model-name id))
            o)
          (get-model* model-name id)))
      (throw (js/Error. (str id " is not a string"))))
    (log/warning *logger* "id is undefined")))

;; Models

(defn initializer
  "used for logging initialization of a model"
  [m coll]
  (this-as this
    (let [n (.-type this)]
      (log/finer *logger* (format "Creating record: %s%s" n (.stringify js/JSON m))))))


(def Model
  (.extend
   backbone/Model
   (js-obj
    "initialize" initializer
    "stub" "STUB"
    "idAttribute" "_id"
    "fetch" (fn []
              (this-as this
                (let [model-name (first
                                  (first
                                   (filter
                                    (fn [[_ _ class-name]]
                                      (= class-name (.-type this)))
                                    names)))]
                  (ws/send "get-model" model-name (.-id this)))))
    "url" (fn [] (this-as this
                   (format "/model/%s/%s.model" (.-stub this) (.-id this)))))))




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

(def Page
  (.extend
   Model
   (js-obj
    "type" "Page"
    "idAttribute" "id"
    "defaults" (fn [] (js-obj
                       "page"         1
                       "loaded" false
                       "pageSize"     0
                       "items"        (array) #_(backbone/Collection.)
                       "recordCount"  0
                       "totalRecords" 0))
    "addItem" (fn [id]
                (this-as this
                  (let [a (.get this "items")]
                    (.set this "items" (clj->js (concat [id] a))))))
    "popItem" (fn []
                (this-as this
                  (let [a (.get this "items")
                        i (first a)]
                    (.set this "items" (clj->js (rest a)))
                    i)))
    "fetch" (fn []
              (this-as this
                (when-not (.-loaded this)
                  (aset this "loaded" true)
                  (ws/send "get-page" (.get this "id")))))
    "hasNext" (fn []
                (this-as this
                  (< (* (.page this)
                        (.pageSize this))
                     (.totalRecords this)))))))

(def SubPage
  (.extend
   Page
   (js-obj
    "type" "SubPage"

    )))

(def Notification
  (.extend
   Model
   (js-obj
    "type" "Notification"
    "dismiss" (fn []
                (this-as this
                  (.remove (.-collection this) this)))
    "default" (js-obj
               "message" ""
                       "loaded" false
               "level"   ""))))

(def Domain
  (.extend
   Model
   (js-obj
    "type" "Domain"
    "stub" "domains"
    "defaults" (js-obj "xmpp"       "unknown"
                       "discovered" nil
                       "loaded" false
                       "created"    nil
                       "updated"    nil
                       "links"      (array)))))

(def Resource
  (.extend
   Model
   (js-obj
    "type" "Resource"
    "stub" "resources"
    "defaults" (js-obj
                "url"         nil
                "title"       nil
                "domain"      nil
                       "loaded" false
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
   Model
   (js-obj
    "type" "User"
    "stub" "users"
    "defaults" (js-obj "url"          nil
                       "avatarUrl"    nil
                       "uri"          nil
                       "bio"          nil
                       "username"     nil
                       "location"     ""
                       "loaded" false
                       "local"        false
                       "domain"       nil
                       "updateSource" nil
                       "links"        (array)
                       "displayName"  nil))))

(def Activity
  (.extend
   Model
   (js-obj
    "type" "Activity"
    "stub" "activities"
    "defaults"
    (fn []
      (js-obj
       "_id"           ""
       "author"        ""
       "uri"           ""
       "url"           nil
       "links"         (array)
       "loaded"        false
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
       "object"        (js-obj
                        "type" nil)
       "like-count"    0
       "updateSource" nil
       "enclosures"    (array))))))

(def Group
  (.extend
   Model
   (js-obj
    "type" "Group"
    "stub" "groups"
    "defaults" (js-obj
                "from"     nil
                "to"       nil
                       "loaded" false
                "homepage" ""
                "fullname" ""
                "nickname" ""))))

(def Subscription
  (.extend
   Model
   (js-obj
    "type" "Subscription"
    "stub" "subscriptions"
    "defaults" (js-obj
                "from"    nil
                "to"      nil
                       "loaded" false
                "created" nil
                "pending" nil
                "local"   nil))))

(def FeedSource
  (.extend
   Model
   (js-obj
    "type" "FeedSource"
    "stub" "feed-sources"
    "defaults" (js-obj
                "callback" nil
                "created"  nil
                       "loaded" false
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
   Model
   (js-obj
    "type" "FeedSubscription"
    "stub" "feed-subscriptions"
    "defaults" (js-obj
                "domain"   nil
                       "loaded" false
                "callback" nil
                "url"      nil))))

(def Conversation
  (.extend
   Model
   (js-obj
    "type" "Conversation"
    "stub" "conversations"
    "defaults" (js-obj
                "uri"           nil
                "url"           nil
                "domain"        nil
                       "loaded" false
                "update-source" nil
                "lastUpdated"   nil
                "created"       nil
                "activities"    nil
                "updated"       nil))))

(def AuthenticationMechanism
  (.extend
   Model
   (js-obj
    "type"        "AuthenticationMechanism"
    "stub"        "authenticationMechanisms"
    "defaults" (js-obj
                "user" nil
                       "loaded" false
                "value" nil))))

;; Collections

(def ^{:doc "collection of activities"}
  Activities
  (.extend backbone/Collection
           (js-obj
            "type" "Activities"
            "urlRoot" "/main/notices/"
            "model" Activity)))

(def AuthenticationMechanisms
  (.extend backbone/Collection
           (js-obj
            "type" "AuthenticationMechanisms"
            "model" AuthenticationMechanism)))

(def Conversations
  (.extend backbone/Collection
           (js-obj
            ;; "localStroage" (js/Store. "conversations")
            "type"         "Conversations"
            "model"        Conversation)))

(def Domains
  (.extend backbone/Collection
           (js-obj
            "type"    "Domains"
            "urlRoot" "/main/domains/"
            "model"   Domain)))

(def FeedSources
  (.extend backbone/Collection
           (js-obj
            "type" "FeedSources"
            "model" FeedSource)))

(def FeedSubscriptions
  (.extend backbone/Collection
           (js-obj
            "type" "FeedSubscription"
            "model" FeedSubscription)))

(def Groups
  (.extend backbone/Collection
           (js-obj
            "model" Group
            "type" "Groups")))

(def Notifications
  (.extend
   backbone/Collection
   (js-obj
    "model" Notification
    "type" "Notifications"

    ;; Add a new notification
    "addNotification"
    (fn [message]
      (this-as this
        (let [notification (Notification.)]
          (.set notification "message" message)
          (.push this notification)))))))

(def Pages
  (.extend
   backbone/Collection
   (js-obj
    "model" Page
    "type" "Pages"
    "getPage" (fn [name]
                (this-as this
                  (.get this name))))))

(def Resources
  (.extend backbone/Collection
           (js-obj
            "type"    "Resources"
            "model"   Resource)))

(def Subscriptions
  (.extend backbone/Collection
           (js-obj
            "model" Subscription
            "type" "Subscriptions")))

(def Users
  (.extend backbone/Collection
           (js-obj
            ;; "localStorage" (js/Store. "users")
            "type"         "Users"
            "model"        User)))

;; Collection references
;; TODO: remove

(def activities    (Activities.))
(def authentication-mechanisms (AuthenticationMechanisms.))
(def conversations  (Conversations.))
(def domains       (Domains.))
(def feed-sources  (FeedSources.))
(def feed-subscriptions (FeedSubscriptions.))
(def groups        (Groups.))
(def notifications (Notifications.))
(def pages         (Pages.))
(def sub-pages     (SubPages.))
(def resources     (Resources.))
(def subscriptions (Subscriptions.))
(def users         (Users.))

;; Viewmodel

(def ^{:doc "The main view model for the site"} AppViewModel
  (let [defaults (js-obj
                  "activities"               activities
                  "authenticationMechanisms" authentication-mechanisms
                  "conversations"            conversations
                  "domains"                  domains
                  "currentUser"              nil
                  "feedSources"              feed-sources
                  "feedSubscriptions"        feed-subscriptions
                  "followers"                (array)
                  "following"                (array)
                  "formats"                  (array)
                  "groups"                   groups
                  "pages"                    pages
                  "subPages"                 sub-pages
                  "postForm"                 (PostForm.)
                  "notifications"            notifications
                  "resources"                resources
                  "statistics"               nil
                  "subscriptions"            subscriptions
                  "targetActivity"           nil
                  "targetConversation"       nil
                  "targetDomain"             nil
                  "targetFeedSource"         nil
                  "targetGroup"              nil
                  "targetResource"           nil
                  "targetUser"               nil
                  "title"                    nil
                  "users"                    users)]
    ;; (doseq [[model-name collection-name class-name] names]
    ;;   (aset defaults name nil))
    (.extend backbone/Model
             (js-obj
              "defaults" defaults))))


;; Page operations

(defn create-page
  [name]
  (log/fine *logger* (format "Creating page: %s" name))
  (.add pages (js-obj "id" name))
  (when-let [page (.get pages name)]
    (log/finer *logger* (format "Fetching page: %s" name))
    (.fetch page)
    page))

(defn create-sub-page
  [model-name id name]
  (log/fine *logger* (format "Creating sub page: %s(%s) => %s" model-name id name))
  (.add sub-pages (js-obj "id" name))
  (when-let [sub-page (.get sub-pages name)]
    (log/finer *logger* (format "Fetching sub page: %s(%s) => %s" name))
    (.fetch sub-page)
    sub-page))

(defn get-page
  "Returns the page for the name from the view's page info.

Returns a viewmodel"
  [name]
  (log/fine *logger* (str "getting page: " name))
  (if-let [page (-> (.pages _view)
                    (.filter (by-name name))
                    first)]
    (do
      (log/finer *logger* "found")
      page)
    (do
      (create-page name)
      ;; TODO: return the observable
      (if-let [found (-> (.pages _view)
                         (.filter (by-name name))
                         first)]
        (do
          (log/finer *logger* "created")
          found)
        (log/fine *logger* "could not find page even after creating")))))

(defn get-sub-page
  "Returns the page for the name from the view's page info.

Returns a viewmodel"
  [model-name id name]
  (log/fine *logger* (format "getting sub page: %s(%s) => %s" model-name id name))
  (if-let [page (-?> (.subPages _view)
                     (aget model-name)
                     (aget id)
                     (.filter (by-name name))
                     first)]
    (do
      (log/finer *logger* "found")
      page)
    (do
      (create-sub-page model-name id name)
      ;; TODO: return the observable
      (if-let [found (-?> (.subPages _view)
                          (aget model-name)
                          (aget id)
                          (.filter (by-name name))
                          first)]
        (do
          (log/finer *logger* "created")
          found)
        (log/fine *logger* "could not find sub page even after creating")))))
