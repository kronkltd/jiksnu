(ns jiksnu.model.user
  (:use (ciste config
               [debug :only [spy]])
        [clj-gravatar.core :only [gravatar-image]]
        jiksnu.model)
  (:require (jiksnu [abdera :as abdera]
                    [namespace :as namespace])
            [clojure.string :as string]
            (clojure.tools [logging :as log])
            (clj-tigase [core :as tigase]
                        [element :as element])
            [karras.entity :as entity]
            [jiksnu.model.domain :as model.domain])
  (:import jiksnu.model.Domain
           jiksnu.model.User
           tigase.xmpp.BareJID
           tigase.xmpp.JID))

(defn salmon-link
  [user]
  (str "http://" (:domain user) "/main/salmon/user/" (:_id user)))

;; TODO: Move this to actions and make it find-or-create
(defn get-domain
  [^User user]
  (model.domain/fetch-by-id (:domain user)))

(defn find-record
  [options]
  
  )

(defn get-uri
  ([^User user] (get-uri user true))
  ([^User user use-scheme?]
     (str (when use-scheme? "acct:") (:username user) "@" (:domain user))))

(defn local?
  [^User user]
  (or (:local user)
      (:local (get-domain user))
      ;; TODO: remove this clause
      (= (:domain user) (config :domain))))

(defn rel-filter
  "returns all the links in the collection where the rel value matches the
   supplied value"
  [rel links]
  (filter #(= (:rel %) rel) links))

(defn split-uri
  "accepts a uri in the form of username@domain or scheme:username@domain and
   returns a vector containing both the username and the domain"
  [uri]
  (let [[_ _ username domain] (re-find #"(.*:)?([^@]+)@([^\?]+)" uri)]
    (when (and username domain) [username domain])))

(defn display-name
  [^User user]
  (or (:display-name user)
      (when (and (:first-name user) (:last-name user))
        (str (:first-name user) " " (:last-name user)))
      (get-uri user)))

(defn get-link
  [user rel]
  (first (rel-filter rel (:links user))))

(defn drop!
  []
  (entity/delete-all User))

(defn create
  [user]
  (if user
    (let [{:keys [username domain]} user]
      (if (and (and username (not= username ""))
               (and domain (not= domain "")))
        (entity/create User user)
        (throw (IllegalArgumentException.
                "Users must contain both a username and a domain"))))
    (throw (IllegalArgumentException. "Can not create nil users"))))

;; deprecated
(defn index
  [& opts]
  (entity/fetch-all User))

(defn fetch-all
  "Fetch all users"
  ([] (fetch-all {}))
  ([params & options]
     (apply entity/fetch User params options))) 

(defn get-user
  "Find a user by username and domain"
  ([username] (get-user username (config :domain)))
  ([username domain]
     (entity/fetch-one
      User
      {:username username
       :domain domain})))

(defn fetch-by-id
  "Fetch a user by it's object id"
  [id]
  (when id
    (try
      (entity/fetch-by-id User id)
      (catch IllegalArgumentException ex
        ;; Invalid ObjectID simply returning nil
        ))))

;; deprecated
;; TODO: Split the jid into it's parts and fetch.
(defn fetch-by-jid
  [jid]
  (get-user (.getLocalpart jid)
            (.getDomain jid)))

(defn set-field
  [user field value]
  (entity/find-and-modify
   User
   {:_id (:_id user)}
   {:$set {field value}}))

(defn fetch-by-uri
  [uri]
  (apply get-user (split-uri uri)))

(defn fetch-by-remote-id
  [uri]
  (entity/fetch-one User {:id uri}))

;; TODO: Is this needed?
(defn subnodes
  [^BareJID user subnode]
  (let [id (tigase/get-id user)
        domain (tigase/get-domain user)]
    (:nodes (get-user id))))

(defn delete
  [id]
  (entity/delete (fetch-by-id id)))

;; TODO: Is this needed?
(defn add-node
  [^User user name]
  (entity/update User
                 {:_id (tigase/get-id user)}))

(defn update
  [^User new-user]
  (let [old-user (get-user (:username new-user) (:domain new-user))
        merged-user (merge {:admin false :debug false}
                           old-user new-user)
        user (entity/make User merged-user)]
    (entity/update User {:_id (:_id old-user)} (dissoc user :_id))
    user))

(defn user-meta-uri
  [^User user]
  (let [domain (get-domain user)]
    (if-let [lrdd-link (get-link domain "lrdd")]
      (let [template (:template lrdd-link)]
        (string/replace template "{uri}" (get-uri user))))))

(defn image-link
  [user]
  (or (:avatar-url user)
      (and (:email user) (gravatar-image (:email user)))
      (gravatar-image (:jid user))
      (gravatar-image (get-uri user)) ""))

(defn vcard-request
  [user]
  (let [body (element/make-element
              "query" {"xmlns" namespace/vcard-query})
        packet-map {:from (tigase/make-jid "" (config :domain))
                    :to (tigase/make-jid user)
                    :id "JIKSNU1"
                    :type :get
                    :body body}]
    (tigase/make-packet packet-map)))
