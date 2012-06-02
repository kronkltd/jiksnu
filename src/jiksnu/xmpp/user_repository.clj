(ns jiksnu.xmpp.user-repository
  (:use [ciste.config :only [config]]
        [ciste.core :only [with-context]]
        [ciste.sections.default :only [show-section]])
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

(defn key-seq
  [subnode key]
  (let [subnodes (if subnode (string/split subnode #"/"))
        ks (map keyword (conj (vec subnodes) key))]
    ks))

(defn find-user
  [^BareJID jid]
  (when (not= (.getDomain jid) "vhost-manager")
    (or (model.user/fetch-by-jid jid)
        (throw (UserNotFoundException.
                (str "Could not find user for " jid))))))

(defmulti get-data (fn [user ks def] ks))

(defmethod get-data [:password]
  [user ks def]
  (log/infof "password handler - %s - %s" (pr-str ks) def)
  (:password user))


(defmethod get-data [:public :vcard-temp :vCard]
  [user ks def]
  (log/info "Vcard handler")
  (with-context [:xmpp :xmpp]
    (show-section user)))

(defmethod get-data :default
  [user ks def]
  (log/infof "default handler - %s - %s" (pr-str ks) def)
  (get-in
   (mc/find-one-as-map "nodes"
                       {:_id (model.user/get-uri user false)})
   ks def))


;; AuthRepository

(defn -addUser
  "This addUser method allows to add new user to repository."
  ([this ^BareJID user]
     (log/info "add user")
     (let [username (.getLocalpart user)
           domain (.getDomain user)]
       (if (and username domain)
         (actions.user/create {:username username :domain domain})
         (if domain
           (when-not (#{"vhost-manager"} domain)
             (actions.domain/find-or-create domain))
           (throw (RuntimeException. "Could not find domain"))))))
  ([this ^BareJID user ^String password]
     (log/info "addUser")
     password))


(defn -digestAuth
  [^AuthRepository this ^BareJID user ^String digest
   ^String id ^String alg]
  (log/info "digest auth")
  (.digestAuth @auth-repository user digest id alg))

(defn ^String -getResourceUri
  "Returns a DB connection string or DB connection URI."
  [^UserRepository this]
  (log/info "get resource uri")
  (cm/implement))

(defn ^long -getUsersCount
  "This method is only used by the server statistics component to report number
of registered users"
  ([^UserRepository this]
     ;; TODO: implement
     (log/info "get users count")
     (count (actions.user/index)))
  ([^UserRepository this ^String domain]
     (log/info "get users count")
     (count (actions.user/index
             {:domain domain}))))

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
  (log/info "other auth")
  (if-let [user (let [mech (.get props AuthRepository/MACHANISM_KEY)]
                  (if (= mech "PLAIN")
                    (let [data (.get props "data")
                          [_ username password] (string/split (String. (model.key/decode data) "UTF-8") #"\u0000")]
                      (actions.auth/login username password))))]
    (do
      (.put props "result" nil)
      (.put props "user-id" (tigase/bare-jid (:username user) (:domain user)))
      true)))

;; plain auth

(defn -queryAuth
  [this props]
  (let [protocol (.get props AuthRepository/PROTOCOL_KEY)]
    (condp = protocol

          AuthRepository/PROTOCOL_VAL_NONSASL (.put props AuthRepository/RESULT_KEY non-sasl-mechs)
          AuthRepository/PROTOCOL_VAL_SASL    (.put props AuthRepository/RESULT_KEY sasl-mechs)

          nil)))

(defn -removeUser
  "allows to remove user and all his data from user repository."
  [this ^BareJID user]
  ;; TODO: implement
  (log/info "remove user")
  (cm/implement))

(defn -updatePassword
  [user password]
  (log/info "update password")
  (cm/implement))



;; UserRepository

(defn -addDataList
  "addDataList method adds mode entries to existing data list associated with
   given key in repository under given node path."
  [this ^BareJID user ^String subnodes list foo]
  ;; TODO: implement
  (log/info "add data list")
  (cm/implement))

;; addUser

(defn ^String -getData
  "returns a value associated with given key for user repository in default
subnode."
  ([^UserRepository this ^BareJID user ^String key]
     (.getData this user nil key nil))
  ([^UserRepository this ^BareJID user ^String subnode ^String key]
     (.getData this user subnode key nil))
  ([^UserRepository this ^BareJID user-id ^String subnode ^String key ^String def]
     (try
       (log/infof "get data - %s - %s" subnode key)
       (let [user (find-user user-id)
             ks (key-seq subnode key)]
         (get-data user ks def))
       (catch Exception ex
         (log/error ex)
         (stacktrace/print-cause-trace ex)))))

(defn -getDataList
  "returns array of values associated with given key or null if given key does
not exist for given user ID in given node path."
  [^UserRepository this ^BareJID user ^String subnode ^String key]
  (log/info "get data list")
  (cm/implement))

(defn -getKeys
  "returns list of all keys stored in given subnode in user repository."
  ([^UserRepository this ^BareJID user-id]
     (.getKeys this user-id nil))
  ([^UserRepository this ^BareJID user ^String subnode]
     (log/info "get keys")
     (cm/implement)))

;; getResourceUri

(defn -getSubnodes
  "returns list of all direct subnodes from given node."
  ([^UserRepository this ^BareJID user-id]
     (.getSubnodes this user-id nil))
  ([^UserRepository this ^BareJID user-id ^String subnode]
     (log/info "get subnodes")
     (cm/implement nil)))

(defn ^long -getUsersUID
  "Returns a user unique ID number within the given repository."
  [this ^BareJID user]
  ;; TODO: implement
  (log/info "get users uid")
  ;; this should return the :_id
  (cm/implement
   user))

(defn -getUsers
  "This method is only used by the data conversion tools."
  [^UserRepository this]
  (log/info "get users")
  ;; TODO: implemen
  ;; Should be get userst
  (actions.user/index))

;; getUsersCount

;; initRepository

(defn -removeData
  "removes pair (key, value) from user repository in given subnode."
  ([this ^BareJID user-id ^String key]
     (.removeData this user-id nil key))
  ([this ^BareJID user-id ^String subnode ^String key]
     (log/info "remove data")
     ;; TODO: implement
     (cm/implement)))

(defn -removeSubnode
  "removes given subnode with all subnodes in this node and all data stored in
this node and in all subnodes."
  [this ^BareJID user
   ^String subnode]
  ;; TODO: implement
  (log/info "remove subnode")
  (cm/implement))

;; removeUser

(defn -setData
  "sets data value for given user ID in repository under given node path and
associates it with given key."
  ([this ^BareJID user ^String key ^String value]
     (.setData this user nil key value))
  ([this ^BareJID user ^String subnode ^String key ^String value]
     ;; TODO: implement
     (log/info "set data - (" user ") " key " = " value)
     (cm/implement)))

(defn -setDataList
  "sets list of values for given user associated given key in repository under
given node path."
  [this ^BareJID user ^String subnode ^String key list]
  (log/info "set data list")
  ;; TODO: implement
  (cm/implement))

(defn -userExists
  "checks whether the user (or repository top node) exists in the database."
  [this ^BareJID user]
  (log/info "user exists")
  (not (nil? (model.user/fetch-by-jid user))))
