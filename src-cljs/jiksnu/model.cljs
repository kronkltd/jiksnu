(ns jiksnu.model
  (:require [inflections.core :as inf]))

(defn deserializer
  "Parses a page response into a js-data format"
  [_resource-name data]
  (if-let [items (.-items (.-data data))]
    items
    (.-data data)))

(defn define-resource
  "Creates a js-data resource with then given name"
  ([DS resource-name]
   (define-resource DS resource-name (inf/plural resource-name)))
  ([DS resource-name endpoint]
   (define-resource DS resource-name endpoint (inf/camel-case resource-name)))
  ([DS resource-name endpoint resource-class]
   (let [methods #js {:getType (constantly resource-class)}
         options #js {:name resource-name :endpoint endpoint :methods methods}]
     (.defineResource DS options))))

(defn Activities       [DS] (define-resource DS "activity"))
(defn Albums           [DS] (define-resource DS "album"))
(defn Clients          [DS] (define-resource DS "client"))
(defn Conversations    [DS] (define-resource DS "conversations"))
(defn Domains          [DS] (define-resource DS "domain"))
(defn FeedSources      [DS] (define-resource DS "feed-source"))
(defn Followings       [DS] (define-resource DS "following"))
(defn Groups           [DS] (define-resource DS "group"))
(defn GroupMemberships [DS] (define-resource DS "group-membership"))
(defn Likes            [DS] (define-resource DS "like"))
(defn Notifications    [DS] (define-resource DS "notification"))
(defn Pages            [DS] (define-resource DS "page"))
(defn Pictures         [DS] (define-resource DS "picture"))
(defn RequestTokens    [DS] (define-resource DS "request-token"))
(defn Resources        [DS] (define-resource DS "resources"))
(defn Streams          [DS] (define-resource DS "stream"))
(defn Services         [DS] (define-resource DS "services"))
(defn Subscriptions    [DS] (define-resource DS "subscription"))

(defn Users
  [DS subpageService]
  (.defineResource
   DS
   (clj->js
    {:name        "user"
     :endpoint    "users"
     :deserialize deserializer
     :methods
     {:getType      (constantly "User")
      :getFollowing (fn [] (this-as item (.fetch subpageService item "following")))}})))
