(ns jiksnu.xmpp.user-repository
  (:use (ciste [config :only [config]]
               [debug :only [spy]]))
  (:require (clojure [stacktrace :as stacktrace]
                     [string :as string])
            (clojure.tools [logging :as log])
            (jiksnu [model :as model])
            (jiksnu.actions [domain-actions :as actions.domain]
                            [user-actions :as actions.user])
            (jiksnu.model [user :as model.user])
            (karras [collection :as col]))
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
(defonce non-sasl-mechs (into-array String ["password" "digest"]))
(defonce sasl-mechs (into-array String ["PLAIN" "DIGEST-MD5" "CRAM-MD5"]))

(defn user-repo
  []
  (col/collection "user-repo"))

(defn -addDataList
  "addDataList method adds mode entries to existing data list associated with
   given key in repository under given node path."
  [this ^BareJID user ^String subnodes list foo]
  ;; TODO: implement
  (log/info "add data list")
  (spy user)
  (spy subnodes)
  (spy list)
  (spy foo)
  )

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
     (spy user)
     (spy password)))

(defn -digestAuth
  [^AuthRepository this ^BareJID user ^String digest
   ^String id ^String alg]
  (log/info "digest auth")
  (.digestAuth
   @auth-repository user digest id alg))

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
  (log/info "password handler")
  (:password user))

(defmethod get-data :default
  [user ks def]
  (log/info "default handler")
  (get-in
   (col/fetch-one (user-repo)
                  {:_id (model.user/get-uri user false)})
   ks def))

(defn ^String -getData
  "returns a value associated with given key for user repository in default
subnode."
  ([^UserRepository this ^BareJID user ^String key]
     (.getData this user nil key nil))
  ([^UserRepository this ^BareJID user ^String subnode ^String key]
     (.getData this user subnode key nil))
  ([^UserRepository this ^BareJID user-id ^String subnode ^String key ^String def]
     ;; TODO: implement
     (try
       (log/info "get data")
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
  ;; TODO: implement
  (log/info "get data list")
  ;; (spy user)
  ;; (spy key)
  ;; (spy subnode)
  nil)

(defn -getKeys
  "returns list of all keys stored in given subnode in user repository."
  ([^UserRepository this ^BareJID user-id]
     (.getKeys this user-id nil))
  ([^UserRepository this ^BareJID user ^String subnode]
     ;; TODO: implement
     (log/info "get keys")
     ;; (spy user)
     ;; (spy subnode)
     nil))

(defn ^String -getResourceUri
  "Returns a DB connection string or DB connection URI."
  [^UserRepository this]
  (log/info "get resource uri")
  ;; TODO: implement
  nil)

(defn -getSubnodes
  "returns list of all direct subnodes from given node."
  ([^UserRepository this ^BareJID user-id]
     (.getSubnodes this user-id nil))
  ([^UserRepository this ^BareJID user-id ^String subnode]
     (log/info "get subnodes")
     ;; (spy user-id)
     ;; (spy subnode)
     nil))

(defn -getUsers
  "This method is only used by the data conversion tools."
  [^UserRepository this]
  (log/info "get users")
  ;; TODO: implement
  (actions.user/index))

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
             {:domain (spy domain)}))))


(defn ^long -getUsersUID
  "Returns a user unique ID number within the given repository."
  [this ^BareJID user]
  ;; TODO: implement
  (log/info "get users uid")
  (spy user))

(defn -initRepository
  "The method is called to initialize the data repository."
  [this ^String resource-uri params]
  ;; TODO: implement
  (log/info "init reposotory")
  (let [auth-repo (AuthRepositoryImpl. this)]
    (dosync
     (ref-set auth-repository auth-repo))))

(defn -otherAuth
  [this props]
  (log/info "other auth")
  (.otherAuth @auth-repository props))

(defn -queryAuth
  [this props]
  (log/info "query auth")
  (.put props "result" (into-array String ["PLAIN"]))
  #_(.queryAuth (spy @auth-repository) (spy props)))

(defn -removeData
  "removes pair (key, value) from user repository in given subnode."
  ([this ^BareJID user-id ^String key]
     (.removeData this user-id nil key))
  ([this ^BareJID user-id ^String subnode ^String key]
     (log/info "remove data")
     ;; TODO: implement
     ;; (spy user-id)
     ;; (spy key)
     ;; (spy subnode)
     ))

(defn -removeSubnode
  "removes given subnode with all subnodes in this node and all data stored in
this node and in all subnodes."
  [this ^BareJID user
   ^String subnode]
  ;; TODO: implement
  (log/info "remove subnode")
  ;; (spy user)
  ;; (spy subnode)
  )

(defn -removeUser
  "allows to remove user and all his data from user repository."
  [this ^BareJID user]
  ;; TODO: implement
  (log/info "remove user")
  ;; (spy user)
  )

(declare get-id)
(declare get-domain)
(declare set-key)

(defn -setData
  "sets data value for given user ID in repository under given node path and
associates it with given key."
  ([this ^BareJID user ^String key ^String value]
     (.setData this user nil key value))
  ([this ^BareJID user ^String subnode ^String key ^String value]
     ;; TODO: implement
     (log/info "set data")
     ;; (spy user)
     ;; (spy subnode)
     ;; (spy key)
     ;; (spy value)

     ))


(defn -setDataList
  "sets list of values for given user associated given key in repository under
given node path."
  [this ^BareJID user ^String subnode ^String key list]
  (log/info "set data list")
  ;; TODO: implement
  )

(defn -updatePassword
  [user password]
  (log/info "update password")
  ;; (spy user)
  ;; (spy password)
  )

(defn -userExists
  "checks whether the user (or repository top node) exists in the database."
  [this ^BareJID user]
  (log/info "user exists")
  (not (nil? (model.user/fetch-by-jid user))))
