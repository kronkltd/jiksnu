(ns jiksnu.model.user
  (:use jiksnu.model)
  (:require [clojure.string :as string]
            [karras.entity :as entity]
            [jiksnu.model.domain :as model.domain])
  (:import tigase.xmpp.BareJID
           jiksnu.model.Domain
           jiksnu.model.User))

(defn get-id
  [jid]
  (.getLocalpart jid))

(defn get-domain
  [^BareJID user]
  (.getDomain user))

(defn bare-jid
  [local domain]
  (BareJID/bareJIDInstance local domain))

(defn split-uri
  [uri]
  (string/split uri #"@"))

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
  (entity/fetch-by-id User id))

(defn fetch-by-jid
  [jid]
  (show (.getLocalpart jid) (.getDomain jid)))

(defn fetch-by-uri
  [uri]
  (apply show (split-uri uri)))

(defn find-or-create
  [username domain]
  (if-let [user (show username domain)]
      user
      (create {:username username :domain domain})))

(defn find-or-create-by-uri
  [uri]
  (apply find-or-create (split-uri uri)))

(defn find-or-create-by-jid
  [jid]
  (find-or-create (.getLocalpart jid) (.getDomain jid)))

(defn subnodes
  [^BareJID user subnode]
  (let [id (get-id user)
        domain (get-domain user)]
    (:nodes (show id))))

(defn edit
  [id]
  (show id))

(defn delete
  [id]
  (entity/delete (fetch-by-id id)))

(defn add-node
  [user name]
  (entity/update User
          {:_id (get-id user)}))

(defn inbox
  []
  #_(model.activity/index)
  [])

(defn update
  [new-user]
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
