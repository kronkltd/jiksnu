(ns jiksnu.xmpp.user-repository
  (:use [ciste.config :only [config]]
        [ciste.core :only [with-context]]
        [ciste.sections.default :only [show-section]])
  (:require [ciste.model :as cm]
            [clj-tigase.core :as tigase]
            [clojure.stacktrace :as stacktrace]
            [clojure.string :as string]
            [clojure.tools.logging :as log]
            [jiksnu.model :as model]
            [jiksnu.ops :as ops]
            [jiksnu.util :as util]
            [lamina.core :as l]
            [monger.collection :as mc])
  (:import tigase.db.AuthorizationException
           tigase.db.AuthRepository
           tigase.db.AuthRepositoryImpl
           tigase.db.UserNotFoundException
           tigase.db.UserRepository
           tigase.xmpp.BareJID)
  (:gen-class
   :implements [tigase.db.AuthRepository
                tigase.db.UserRepository]))

(defonce auth-repository (ref nil))
(defonce password-key "password")
(defonce non-sasl-mechs (into-array String ["password"]))
(defonce sasl-mechs (into-array String ["PLAIN"]))

(defmulti get-data (fn [user ks def] ks))

(defonce add-user-ch (l/channel* :transactional? true :permanent? true))
(defonce get-data-ch (l/channel* :transactional? true :permanent? true))
(defonce count-users-ch (l/channel* :transactional? true :permanent? true))
(defonce other-auth-ch (l/channel* :transactional? true :permanent? true))
(defonce user-exists-ch (l/channel* :transactional? true :permanent? true))


;; AuthRepository

(defn -addUser
  "This addUser method allows to add new user to repository."
  [this ^BareJID user & [^String password]]
  (ops/async-op add-user-ch [user password]))


(defn -digestAuth
  [^AuthRepository this ^BareJID user ^String digest
   ^String id ^String alg]
  (log/info "digest auth")
  (.digestAuth @auth-repository user digest id alg))

(defn ^long -getUsersCount
  "This method is only used by the server statistics component to report number
of registered users"
  [^UserRepository this & [^String domain]]
  (ops/async-op count-users-ch domain))

(defn -initRepository
  "The method is called to initialize the data repository."
  [this ^String resource-uri params]
  ;; TODO: implement
  (log/info "init reposotory")
  (let [auth-repo (AuthRepositoryImpl. this)]
    (dosync
     (ref-set auth-repository auth-repo))))

;; logout

(defn -otherAuth
  [this props]
  (ops/async-op count-users-ch props))

;; plain auth

(defn -queryAuth
  [this props]
  (let [protocol (.get props AuthRepository/PROTOCOL_KEY)]
    (condp = protocol

          AuthRepository/PROTOCOL_VAL_NONSASL (.put props AuthRepository/RESULT_KEY non-sasl-mechs)
          AuthRepository/PROTOCOL_VAL_SASL    (.put props AuthRepository/RESULT_KEY sasl-mechs)

          nil)))

;; addUser

(defn ^String -getData
  "returns a value associated with given key for user repository in default
subnode."
  ([^UserRepository this ^BareJID user ^String key]
     (.getData this user nil key nil))
  ([^UserRepository this ^BareJID user ^String subnode ^String key]
     (.getData this user subnode key nil))
  ([^UserRepository this ^BareJID user-id ^String subnode ^String key ^String def]
     (ops/async-op get-data-ch [user-id subnode key def])))

(defn -userExists
  "checks whether the user (or repository top node) exists in the database."
  [this ^BareJID user]
  (ops/async-op user-exists-ch [user]))
