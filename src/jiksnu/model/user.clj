(ns jiksnu.model.user
  (:use (ciste config
               [debug :only (spy)])
        [clj-gravatar.core :only (gravatar-image)]
        jiksnu.model)
  (:require [jiksnu.abdera :as abdera]
            [clojure.string :as string]
            (clojure.tools [logging :as log])
            (clj-tigase [core :as tigase])
            [karras.entity :as entity]
            [jiksnu.model.domain :as model.domain])
  (:import jiksnu.model.Domain
           jiksnu.model.User
           tigase.xmpp.BareJID
           tigase.xmpp.JID))

(defn get-domain
  [^User user]
  (model.domain/show (:domain user)))

(defn get-uri
  ([^User user] (get-uri user true))
  ([^User user use-scheme?]
     (str (if use-scheme? "acct:") (:username user) "@" (:domain user))))

(defn local?
  [^User user]
  (or (:local user)
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
    (if (and username domain) [username domain])))

(defn display-name
  [^User user]
  (or (:display-name user)
      (if (and (:first-name user) (:last-name user))
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

(defn index
  [& opts]
  (entity/fetch-all User))

(defn show
  ([username] (show username (config :domain)))
  ([username domain]
     (entity/fetch-one
      User
      {:username username
       :domain domain})))

(defn fetch-by-id
  [id]
  (if id
   (entity/fetch-by-id User id)))

(defn fetch-by-jid
  [jid]
  (show (.getLocalpart jid) (.getDomain jid)))

(defn fetch-by-uri
  [uri]
  (apply show (split-uri uri)))

(defn fetch-by-remote-id
  [uri]
  (entity/fetch-one User {:id uri}))

(defn find-or-create
  [username domain]
  (if-let [user (show username domain)]
      user
      (create {:username username :domain domain})))

(defn find-or-create-by-uri
  [uri]
  (apply find-or-create (split-uri uri)))

(defn find-or-create-by-remote-id
  [id]
  (or (fetch-by-remote-id id)
      (create {:id id})))

(defn find-or-create-by-jid
  [^JID jid]
  (find-or-create (tigase/get-id jid) (tigase/get-domain jid)))

;; TODO: Is this needed?
(defn subnodes
  [^BareJID user subnode]
  (let [id (tigase/get-id user)
        domain (tigase/get-domain user)]
    (:nodes (show id))))

(defn edit
  [id]
  (show id))

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
  (let [old-user (show (:username new-user) (:domain new-user))
        merged-user (merge old-user
                           ;; If these fields are unchecked, they
                           ;; won't be sent. These fields must be sent
                           ;; every time.
                           {:admin false :debug false}
                           new-user)
        user (entity/make User merged-user)]
    (entity/update User {:_id (:_id old-user)} user)
    user))

(defn user-meta-uri
  [^User user]
  (let [domain (get-domain user)]
    (if-let [lrdd-link (get-link domain "lrdd")]
      (let [template (:template lrdd-link)]
        (string/replace template "{uri}" (get-uri user))))))

(defn format-data
  [^User user]
  (let [{id :_id
         :keys [username domain local hub admin]} user
         uri (get-uri user)]
    {:id (str id)
     :name uri
     :username username
     :domain domain
     :url (if (local? user)
            (str "/" username)
            (str "/remote-user/" uri))
     :local local
     :hub hub
     :admin admin
     :links []
     :subscriptions []
     :subscribers []
     :display-name (display-name user)
     :imgsrc (or (:avatar-url user)
                 (and (:email user) (gravatar-image (:email user)))
                 (gravatar-image (:jid user))
                 (gravatar-image uri) "")}))
