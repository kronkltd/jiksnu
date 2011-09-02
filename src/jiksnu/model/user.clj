(ns jiksnu.model.user
  (:use (ciste config debug)
        [clj-gravatar.core :only (gravatar-image)]
        jiksnu.model)
  (:require [jiksnu.abdera :as abdera]
            [clojure.string :as string]
            [karras.entity :as entity]
            [jiksnu.model.domain :as model.domain])
  (:import jiksnu.model.Domain
           jiksnu.model.User
           tigase.xmpp.JID))

(defn split-uri
  [uri]
  (string/split uri #"@"))

(defn rel-filter
  [rel links]
  (filter #(= (:rel %) rel)
          links))

(defn get-link
  [user rel]
  (first (rel-filter rel (:links user))))

(defn drop!
  []
  (entity/delete-all User))

(defn create
  [user]
  (let [domain (model.domain/find-or-create (:domain user))]
    (entity/create User user)))

(defn index
  [& opts]
  (entity/fetch-all User))

(defn show
  ([id]
     (show id nil))
  ([username domain]
     (let [opt-map
           (merge {:username username}
                  (if domain
                    {:domain domain}))]
       (entity/fetch-one User opt-map))))

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
  (find-or-create (abdera/get-id jid) (abdera/get-domain jid)))

;; TODO: Is this needed?
(defn subnodes
  [^BareJID user subnode]
  (let [id (abdera/get-id user)
        domain (abdera/get-domain user)]
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
          {:_id (abdera/get-id user)}))

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

(defn local?
  [^User user]
  (= (:domain user) (config :domain)))

(defn get-uri
  [^User user]
  (str (:username user) "@" (:domain user)))

(defn get-domain
  [^User user]
  (model.domain/show (:domain user)))

(defn user-meta-uri
  [^User user]
  (let [domain-object (abdera/get-domain user)]
    (if-let [lrdd-link (get-link domain-object "lrdd")]
      (let [template (:template lrdd-link)]
        (string/replace template "{uri}" (get-uri user))))))

(defn display-name
  [^User user]
  (or (:display-name user)
      (if (and (:first-name user) (:last-name user))
        (str (:first-name user) " " (:last-name user)))
      (get-uri user)))

(defn format-data
  [^User user]
  (let [{id :_id
         :keys [username domain local hub admin]} user
         uri (get-uri user)]
    {:id (str id)
     :name uri
     :username username
     :domain domain
     :url (if (:local user)
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
