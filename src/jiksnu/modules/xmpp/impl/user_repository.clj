(ns jiksnu.modules.xmpp.impl.user-repository
  (:use [ciste.config :only [config]]
        [ciste.core :only [with-context]]
        [ciste.sections.default :only [show-section]]
        [slingshot.slingshot :only [try+]])
  (:require [ciste.model :as cm]
            [clj-tigase.core :as tigase]
            [clojure.stacktrace :as stacktrace]
            [clojure.string :as string]
            [clojure.tools.logging :as log]
            [jiksnu.actions.auth-actions :as actions.auth]
            [jiksnu.actions.domain-actions :as actions.domain]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.model :as model]
            [jiksnu.model.key :as model.key]
            [jiksnu.model.user :as model.user]
            [jiksnu.ops :as ops]
            [lamina.core :as l]
            [lamina.trace :as trace]
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

(defmulti get-datam (fn [user ks def] ks))

(defonce add-user-ch (l/channel* :transactional? true :permanent? true))
(defonce get-data-ch (l/channel* :transactional? true :permanent? true))
(defonce count-users-ch (l/channel* :transactional? true :permanent? true))
(defonce other-auth-ch (l/channel* :transactional? true :permanent? true))
(defonce user-exists-ch (l/channel* :transactional? true :permanent? true))


(defn init-repository
  [this resource-uri params]
    (let [auth-repo (AuthRepositoryImpl. this)]
    (dosync
     (ref-set auth-repository auth-repo)))
  )

;; (defn add-user
;;   [^BareJID user-id & [password]]
;;   (ops/async-op add-user-ch [user password])
;;   )

;; (defn digest-auth
;;   [^AuthRepository this ^BareJID user ^String digest
;;    ^String id ^String alg]
;;   (log/info "digest auth")
;;   (.digestAuth @auth-repository user digest id alg))

(defn user-exists
  [user]
  (ops/async-op user-exists-ch [user]))

(defn key-seq
  [subnode key]
  (let [subnodes (if subnode (string/split subnode #"/"))
        ks (map keyword (conj (vec subnodes) key))]
    ks))

(defn other-auth
  [props]
  (ops/async-op count-users-ch props))

(defn query-auth
  [props]
  (let [protocol (.get props AuthRepository/PROTOCOL_KEY)]
    (condp = protocol

          AuthRepository/PROTOCOL_VAL_NONSASL (.put props AuthRepository/RESULT_KEY non-sasl-mechs)
          AuthRepository/PROTOCOL_VAL_SASL    (.put props AuthRepository/RESULT_KEY sasl-mechs)

          nil))
  )

(defn find-user
  [^BareJID jid]
  (when (not= (.getDomain jid) "vhost-manager")
    (or (model.user/fetch-by-jid jid)
        (throw (UserNotFoundException.
                (str "Could not find user for " jid))))))

(defmethod get-datam [:password]
  [user ks def]
  (log/infof "password handler - %s - %s" (pr-str ks) def)
  (:password user))

(defmethod get-datam [:public :vcard-temp :vCard]
  [user ks def]
  (log/info "Vcard handler")
  (with-context [:xmpp :xmpp]
    (show-section user)))

(defmethod get-datam :default
  [user ks def]
  (log/infof "default handler - %s - %s" (pr-str ks) def)
  (get-in
   (mc/find-one-as-map "nodes"
                       {:_id (model.user/get-uri user false)})
   ks def))

(defn get-data
  [^BareJID user-id ^String subnode ^String key ^String def]
  (ops/async-op get-data-ch [user-id subnode key def]))

(defn get-user-count
  [& [domain]]
  (ops/async-op count-users-ch domain))

(defn handle-add-user
  [[result [^BareJID bare password]]]
  (log/info "add user")
  (deliver result
           (let [username (.getLocalpart bare)
                 domain (.getDomain bare)]
             (if (and username domain)
               (actions.user/create {:username username :domain domain})
               (if domain
                 (when-not (#{"vhost-manager"} domain)
                   @(ops/get-discovered domain))
                 #_(throw+ "Could not find domain"))))))

(defn handle-count-users
  [[result domain]]
  (deliver result
           (if domain
             (model.user/count-records {:domain domain})
             (model.user/count-records))))

(defn handle-other-auth
  [[result props]]
  (deliver result
           (if-let [user (let [mech (.get props AuthRepository/MACHANISM_KEY)]
                           (if (= mech "PLAIN")
                             (let [data (.get props "data")
                                   [_ username password] (string/split (String. (model.key/decode data) "UTF-8") #"\u0000")]
                               (if-let [user (model.user/get-user username)]
                                 (actions.auth/login user password)
                                 (log/warnf "Could not find user with username: %s" username)))))]
             (do
               (.put props "result" nil)
               (.put props "user-id" (tigase/bare-jid (:username user) (:domain user)))
               true))))

(defn handle-get-data
  [[result [^BareJID user-id ^String subnode ^String key ^String def]]]
  (deliver result
           (try+
             (log/infof "get data - %s - %s" subnode key)
             (let [user (find-user user-id)
                   ks (key-seq subnode key)]
               (get-datam user ks def))
             (catch Exception ex
               (trace/trace "errors:handled" ex)))))

(defn handle-user-exists
  [result [user]]
  (log/info "user exists")
  (deliver result (not (nil? (model.user/fetch-by-jid user)))))

(defn init-handlers
  []
  (l/receive-all add-user-ch handle-add-user)
  (l/receive-all get-data-ch handle-get-data)
  (l/receive-all count-users-ch handle-count-users)
  (l/receive-all other-auth-ch handle-other-auth)
  (l/receive-all user-exists-ch handle-user-exists))

(init-handlers)
