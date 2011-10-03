(ns jiksnu.xmpp.user-repository
  (:use (ciste [config :only (config)]
               [debug :only (spy)])
        (jiksnu [model :only (with-database)]))
  (:require (clojure [stacktrace :as stacktrace]
                     [string :as string])
            (clojure.tools [logging :as log])
            (jiksnu [model :as model])
            (jiksnu.actions [domain-actions :as actions.domain]
                            [user-actions :as actions.user])
            (jiksnu.model [user :as model.user])
            (karras [collection :as col]))
  (:import tigase.db.AuthorizationException
           tigase.db.AuthRepositoryImpl
           tigase.db.UserNotFoundException
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
  (with-database
    (col/collection "user-repo")))

(defn -addDataList
  "addDataList method adds mode entries to existing data list associated with
   given key in repository under given node path."
  [this ^BareJID user ^String subnodes list]
  ;; TODO: implement
  (log/info "add data list")
  ;; (spy user)
  ;; (spy subnodes)
  ;; (spy list)
  )

(defn -addUser
  "This addUser method allows to add new user to repository."
  ([this ^BareJID user]
     (log/info "add user")
     (with-database
       (if (spy user)
         (let [username (.getLocalpart user)
               domain (.getDomain user)]
           (if (and (spy username) (spy domain))
             (actions.user/create (spy {:username username
                                        :domain domain}))
             (if (spy domain)
               (actions.domain/find-or-create domain)
               (throw (RuntimeException. "Could not find domain"))))))))
  ([this ^BareJID user ^String password]
     (log/info "addUser")
     (spy user)
     (spy password)
     ))

(defn -digestAuth
  [this user digest id alg]
  (log/info "digest auth")
  (.digestAuth @auth-repository user digest id alg))

(defn key-seq
  [subnode key]
  (let [subnodes (if subnode (string/split subnode #"/"))
        ks (map keyword (conj (vec subnodes) key))]
    ks))

(defn find-user
  [jid]
  (or (model.user/fetch-by-jid jid)
      (throw (UserNotFoundException.
              (str "Could not find user for " jid)))))

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
                  {:_id (str (:username user) "@" (:domain user))})
   ks def))

(defn ^String -getData
  "returns a value associated with given key for user repository in default
subnode."
  ([this ^BareJID user ^String key]
     (.getData this user nil key nil))
  ([this ^BareJID user ^String subnode ^String key]
     (.getData this user subnode key nil))
  ([this ^BareJID user-id ^String subnode ^String key ^String def]
     ;; TODO: implement
     (try
       (with-database
         (log/info "get data")
         (let [user (find-user (spy user-id))
               ks (key-seq subnode key)]
           (spy (get-data user (spy ks) def))))
       (catch Exception ex
         (log/error ex)
         (stacktrace/print-cause-trace ex)))))

(defn -getDataList
  "returns array of values associated with given key or null if given key does
not exist for given user ID in given node path."
  [this ^BareJID user ^String subnode ^String key]
  ;; TODO: implement
  (log/info "get data list")
  ;; (spy user)
  ;; (spy key)
  ;; (spy subnode)
  nil)

(defn -getKeys
  "returns list of all keys stored in given subnode in user repository."
  ([this ^BareJID user-id]
     (.getKeys this user-id nil))
  ([this ^BareJID user ^String subnode]
     ;; TODO: implement
     (log/info "get keys")
     ;; (spy user)
     ;; (spy subnode)
     nil))

(defn ^String -getResourceUri
  "Returns a DB connection string or DB connection URI."
  [this]
  (log/info "get resource uri")
  ;; TODO: implement
  nil)

(defn -getSubnodes
  "returns list of all direct subnodes from given node."
  ([this ^BareJID user-id]
     (.getSubnodes this user-id nil))
  ([this ^BareJID user-id ^String subnode]
     (log/info "get subnodes")
     ;; (spy user-id)
     ;; (spy subnode)
     nil))

(defn -getUsers
  "This method is only used by the data conversion tools."
  [this ]
  (log/info "get users")
  ;; TODO: implement
  (actions.user/index))

(defn ^long -getUsersCount
  "This method is only used by the server statistics component to report number
of registered users"
  ([this]
     ;; TODO: implement
     (with-database
       (log/info "get users count")
       (count (actions.user/index))))
  ([this ^String domain]
     (with-database
       (log/info "get users count")
       (count (actions.user/index
               {:domain (spy domain)})))))


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
  (.queryAuth @auth-repository props))

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
  [#^BareJID user
   #^String subnode]
  ;; TODO: implement
  )

(defn -removeUser
  "allows to remove user and all his data from user repository."
  [#^BareJID user]
  ;; TODO: implement
  )

(declare get-id)
(declare get-domain)
(declare set-key)

(defn -setData
  "sets data value for given user ID in repository under given node path and
associates it with given key."
  ([#^BareJID user
    #^String key
    #^String value]
     ;; TODO: implement
     (let [id (get-id user)
           domain (get-domain user)]
       (set-key user key value)))
  ([#^BareJID user
    #^String subnode
    #^String key
    #^String value]
     ;; TODO: implement
     ))


(defn -setDataList
  "sets list of values for given user associated given key in repository under
given node path."
  [#^BareJID user
   #^String subnode
   #^String key
   list]
  ;; TODO: implement
  )

(defn -userExists
  "checks whether the user (or repository top node) exists in the database."
  [#^BareJID user])
