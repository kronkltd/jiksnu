(ns jiksnu.model)

(defn deserializer
  [resource-name data]
  (if-let [items (.-items (.-data data))]
    items
    (.-data data)))

;; (def.factory jiksnu.$exceptionHandler
;;   []
;;   (fn [exception cause]
;;     (throw exception)))

(defn define-resource
  [DS resource-name endpoint resource-class]
  (.defineResource
   DS
   #js
   {:name resource-name
    :endpoint endpoint
    :methods #js {:getType (constantly resource-class)}}))

(defn Activities       [DS] (define-resource DS "activity"         "activities"        "Activity"))
(defn Albums           [DS] (define-resource DS "album"            "albums"            "Album"))
(defn Clients          [DS] (define-resource DS "client"           "clients"           "Client"))
(defn Domains          [DS] (define-resource DS "domain"           "domains"           "Domain"))
(defn Followings       [DS] (define-resource DS "following"        "followings"        "Following"))
(defn Groups           [DS] (define-resource DS "group"            "groups"            "Group"))
(defn GroupMemberships [DS] (define-resource DS "group-membership" "group-memberships" "GroupMembership"))
(defn Likes            [DS] (define-resource DS "like"             "likes"             "Like"))
(defn Notifications    [DS] (define-resource DS "notification"     "notifications"     "Notification"))
(defn Pages            [DS] (define-resource DS "page"             "pages"             "Page"))
(defn Pictures         [DS] (define-resource DS "picture"          "pictures"          "Picture"))
(defn RequestTokens    [DS] (define-resource DS "request-token"    "request-tokens"    "RequestToken"))
(defn Streams          [DS] (define-resource DS "stream"           "streams"           "Stream"))
(defn Subscriptions    [DS] (define-resource DS "subscription"     "subscriptions"     "Subscription"))

(defn Conversations
  [DS subpageService]
  (.defineResource
   DS
   #js
   {:name "conversation"
    :endpoint "conversations"
    :deserialize deserializer
    :methods
    #js
    {:getActivities (fn [] (this-as item (.fetch subpageService item "activities")))
     :getType (constantly "Conversation")}}))

(defn Users
  [DS subpageService]
  (.defineResource
   DS
   #js
   {:name        "user"
    :endpoint    "users"
    :deserialize deserializer
    :methods
    #js
    {:getType      (constantly "User")
     :getSubpage   (fn [page-name] (this-as item (.fetch subpageService item page-name)))
     :getFollowers (fn [] (this-as item (.fetch subpageService item "followers")))
     :getFollowing (fn [] (this-as item (.fetch subpageService item "following")))
     :getGroups    (fn [] (this-as item (.fetch subpageService item "groups")))
     :getStreams   (fn [] (this-as item (.fetch subpageService item "streams")))}}))
