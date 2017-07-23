(ns jiksnu.modules.core.model
  (:require [clojure.spec :as s]
            [clojure.spec.gen :as gen]
            [clojure.spec.test :as stest]
            [jiksnu.util :as util]))

;; TODO: pull these from ns/
(defonce bound-ns {:hm "http://host-meta.net/xrd/1.0"
                   :xrd "http://docs.oasis-open.org/ns/xri/xrd-1.0"})

;; Any of the set of valid collection names
(s/def ::collection string?)

;; Any of the set of valid record types. (Class-form)
(s/def ::type       string?)

;; The inner item of the models var
(s/def ::model-registry-item (s/keys :req [::collection ::type]))

;; The value of the models var
(s/def ::model-registry (s/map-of string? ::model-registry-item :conform-keys true))


(def models
  {"like" {::collection "likes" ::type "Like"}})

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
(defrecord User                    [_id])

(s/def ::_id
  (s/with-gen string?
    #(gen/fmap (fn [[username domain]] (str "acct:" username "@" domain))
               (gen/tuple (gen/string-alphanumeric) (gen/string-alphanumeric)))))

(s/def ::user-record (s/keys :req-un [::_id]))

(defrecord UserList                [])

(defn get-link
  ([item rel]
   (get-link item rel nil))
  ([item rel content-type]
   (first (util/rel-filter rel (:links item) content-type))))

(s/fdef get-link
        :args (s/cat
               ;; Any record with links
               ;; TODO: create type for records containing links
               :item map?

               ;; rel attribute of link in collection
               :rel string?

               ;; optional content-type attribute
               :content-type (s/? string?))
        :ret string?)

(stest/instrument 'get-link)
