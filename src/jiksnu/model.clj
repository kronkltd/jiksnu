(ns jiksnu.model)

;; TODO: pull these from ns/
(defonce bound-ns {:hm "http://host-meta.net/xrd/1.0"
                   :xrd "http://docs.oasis-open.org/ns/xri/xrd-1.0"})

(defrecord AccessToken             [])
(defrecord Activity                [])
(defrecord ActivityObject          [])
(defrecord AuthenticationMechanism [])
(defrecord Client                  [])
(defrecord Conversation            [])
(defrecord Dialback                [])
(defrecord Domain                  [])
(defrecord FeedSource              [])
(defrecord FeedSubscription        [])
(defrecord Group                   [])
(defrecord GroupMembership         [])
(defrecord Item                    [])
(defrecord Key                     [])
(defrecord Like                    [])
(defrecord RequestToken            [])
(defrecord Resource                [])
(defrecord Stream                  [])
(defrecord Subscription            [])
(defrecord User                    [])
(defrecord UserList                    [])

(def entity-names
  [
   AccessToken
   Activity
   ActivityObject
   AuthenticationMechanism
   Client
   Conversation
   Dialback
   Domain
   FeedSource
   FeedSubscription
   Group
   GroupMembership
   Item
   Key
   Like
   RequestToken
   Resource
   Stream
   Subscription
   User
   UserList
   ]
  )

;; Entity predicates

(defn domain?
  [domain]
  (instance? Domain domain))

(defn subscription?
  [subscription]
  (instance? Subscription subscription))

(defn user?
  "Is the provided object a user?"
  [user] (instance? User user))

