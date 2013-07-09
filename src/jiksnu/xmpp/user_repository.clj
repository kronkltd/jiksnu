(ns jiksnu.xmpp.user-repository
  (:import tigase.db.AuthorizationException
           tigase.db.AuthRepository
           tigase.db.AuthRepositoryImpl
           tigase.db.UserNotFoundException
           tigase.db.UserRepository
           tigase.xmpp.BareJID)
  (:gen-class
   :implements [tigase.db.AuthRepository
                tigase.db.UserRepository]))

(defprotocol UserRepo
  (add-user [this user-id password])
  (digest-auth [this user digest id alg])
  (get-user-count [this domain])
  (init [this resource-uri params])
  (other-auth [this props])
  (query-auth [this props])
  (get-data [this user-id subnode key def])
  (user-exists [this user]))

(deftype NullUserRepo []
    UserRepo

    (add-user [this user-id password])
    )

(defonce ^:dynamic *repo* (NullUserRepo.))

;; AuthRepository

(defn -addUser
  "This addUser method allows to add new user to repository."
  [this ^BareJID user & [^String password]]
  (add-user *repo* user password))

(defn -digestAuth
  [^BareJID user ^String digest
   ^String id ^String alg]
  (digest-auth *repo* user digest id alg))

(defn ^long -getUsersCount
  "This method is only used by the server statistics component to report number
of registered users"
  [^UserRepository this & [^String domain]]
  (get-user-count *repo* domain))

(defn -initRepository
  "The method is called to initialize the data repository."
  [this ^String resource-uri params]
  (init *repo* resource-uri params))

;; logout

(defn -otherAuth
  [this props]
  (other-auth *repo* props))

;; plain auth

(defn -queryAuth
  [this props]
  (query-auth *repo* props))

;; addUser

(defn ^String -getData
  "returns a value associated with given key for user repository in default
subnode."
  ([^UserRepository this ^BareJID user ^String key]
     (.getData this user nil key nil))
  ([^UserRepository this ^BareJID user ^String subnode ^String key]
     (.getData this user subnode key nil))
  ([^UserRepository this ^BareJID user-id ^String subnode ^String key ^String def]
     (get-data *repo* user-id subnode key def)))

(defn -userExists
  "checks whether the user (or repository top node) exists in the database."
  [this ^BareJID user]
  (user-exists *repo* user))
