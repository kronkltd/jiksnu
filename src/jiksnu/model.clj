(ns jiksnu.model
  (:require [jiksnu.util :as util]))

;; TODO: pull these from ns/
(defonce bound-ns {:hm "http://host-meta.net/xrd/1.0"
                   :xrd "http://docs.oasis-open.org/ns/xri/xrd-1.0"})

(defrecord AccessToken             [])
(defrecord Activity                [])
(defrecord ActivityObject          [])
(defrecord Album                   [])
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
(defrecord Notification            [])
(defrecord Picture                 [])
(defrecord RequestToken            [])
(defrecord Resource                [])
(defrecord Service                 [])
(defrecord Stream                  [])
(defrecord Subscription            [])
(defrecord User                    [])
(defrecord UserList                [])

(defn get-link
  ([item rel]
   (get-link item rel nil))
  ([item rel content-type]
   (first (util/rel-filter rel (:links item) content-type))))
