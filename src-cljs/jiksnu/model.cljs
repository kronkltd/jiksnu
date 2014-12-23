(ns jiksnu.model
  (:require [goog.json :as json]
            [jiksnu.websocket :as ws]
            [lolg :as log])
  (:use-macros [purnam.core :only [? ?> ! !> f.n def.n do.n obj arr def* do*n
                                   def*n f*n]]))

(def *logger* (log/get-logger "jiksnu.model"))

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

;; Protocol helpers

(defn extend
  [m1 m2]
  (.extend js/_ m1 m2))

(defn get-model-name
  "Returns the (lower-cased) name of the given model"
  [model]
  (first
   (first
    (filter
     (fn [[_ _ class-name]]
       (= class-name (.-type model)))
     names))))

(defn fetch-model
  "Sends a request for the given Model"
  [model]
  (let [model-name (get-model-name model)]
    ;; TODO: trigger an event to do this
    (ws/send "get-model" (array model-name (.-id model)))))

(defn page-add
  [model id]
  (let [a (.get model "items")]
    (.set model "items" (clj->js (concat [id] a)))))

;; Model helpers

(defn initializer
  "used for logging initialization of a model"
  [m coll]
  (let [n (.-type (js* "this"))]
    (log/finer *logger* (str "Creating record: " n
                             " " (json/serialize m)))))

