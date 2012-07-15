(ns jiksnu.model.user
  (:use [ciste.config :only [config]] 
        [clj-gravatar.core :only [gravatar-image]]
        [jiksnu.model :only [make-id rel-filter map->User]]
        [slingshot.slingshot :only [throw+]]
        [validateur.validation :only [acceptance-of validation-set presence-of]])
  (:require [clojure.string :as string]
            [clojure.tools.logging :as log]
            [clj-tigase.core :as tigase]
            [clj-tigase.element :as element]
            [jiksnu.model :as model]
            [jiksnu.model.domain :as model.domain]
            [jiksnu.namespace :as ns]
            [monger.collection :as mc]
           )
  (:import jiksnu.model.Domain
           jiksnu.model.User
           tigase.xmpp.BareJID
           tigase.xmpp.JID))

(def collection-name "users")
(def default-page-size 20)

(def create-validators
  (validation-set
   (presence-of :username)
   (presence-of :domain)
   (presence-of :id)
   #_(presence-of :local)
   (acceptance-of :username :accept string?)
   (acceptance-of :domain :accept string?)
   ))

(defn salmon-link
  [user]
  (str "http://" (:domain user) "/main/salmon/user/" (:_id user)))

;; TODO: Move this to actions and make it find-or-create
(defn get-domain
  [^User user]
  (model.domain/fetch-by-id (:domain user)))

(defn local?
  [^User user]
  (or (:local user)
      (:local (get-domain user))
      ;; TODO: remove this clause
      (= (:domain user) (config :domain))))

(defn get-uri
  ([^User user] (get-uri user true))
  ([^User user use-scheme?]
     (str (when use-scheme? "acct:") (:username user) "@" (:domain user))))

(defn uri
  "returns the relative path to the user's profile page"
  [user]
  (if (local? user)
    (str "/" (:username user))
    (str "/remote-user/" (get-uri user false))))

(defn full-uri
  "The fully qualified path to the user's profile page on this site"
  [user]
  (str "http://" (config :domain) (uri user)))

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
  [user rel content-type]
  (first (rel-filter rel (:links user) content-type)))

(defn drop!
  []
  (mc/remove collection-name))

(defn fetch-by-id
  "Fetch a user by it's object id"
  [id]
  (if-let [user (mc/find-map-by-id collection-name id)]
    (model/map->User user)
    (log/warnf "Could not find user: %s" id)))

(defn create
  [user]
  (if user
    (let [id (make-id)
          {:keys [username domain]} user]
      (let [errors (create-validators user)]
        (if (empty? errors)
          (let [user (assoc user :_id id)]
            (log/debugf "Creating user: %s" user)
            (mc/insert collection-name user)
            (fetch-by-id id))
          (throw+ {:type :validation
                   :errors errors}))))))

(defn fetch-all
  "Fetch all users"
  ([] (fetch-all {}))
  ([params] (fetch-all params {}))
  ([params options]
     (->> (mc/find-maps collection-name params)
         (map model/map->User)))) 

(defn get-user
  "Find a user by username and domain"
  ([username] (get-user username (config :domain)))
  ([username domain]
     (if-let [user (mc/find-one-as-map collection-name
                                       {:username username
                                        :domain domain})]
       (model/map->User user))))

;; deprecated
;; TODO: Split the jid into it's parts and fetch.
(defn fetch-by-jid
  [jid]
  (get-user (.getLocalpart jid)
            (.getDomain jid)))

(defn set-field!
  "Updates user's field to value"
  [user field value]
  (mc/update collection-name
             {:_id (:_id user)}
             {:$set {field value}}))

(defn fetch-by-uri
  "Fetch user by their acct uri"
  [uri]
  (apply get-user (split-uri uri)))

(defn fetch-by-remote-id
  "Fetch user by their id value"
  [uri]
  (if-let [user (mc/find-one-as-map collection-name {:id uri})]
    (model/map->User user)))

(defn fetch-by-domain
  ([domain] (fetch-by-domain domain {}))
  ([domain options]
     (fetch-all {:domain (:_id domain)}
                {:limit 20})))

;; TODO: Is this needed?
(defn subnodes
  [^BareJID user subnode]
  (let [id (tigase/get-id user)
        domain (tigase/get-domain user)]
    (:nodes (get-user id))))

;; TODO: Should accept a user
(defn delete
  [id]
  (mc/remove-by-id collection-name id))

(defn update
  [^User new-user]
  (log/info "updating user")
  (let [old-user (get-user (:username new-user) (:domain new-user))
        merged-user (merge {:admin false}
                           old-user new-user)
        user (map->User merged-user)]
    (mc/update collection-name {:_id (:_id old-user)} (dissoc user :_id))
    user))

;; TODO: move part of this to domains
(defn user-meta-uri
  [^User user]
  (if-let [domain (get-domain user)]
    (if-let [lrdd-link (get-link domain "lrdd" nil)]
      (let [template (:template lrdd-link)]
        (string/replace template "{uri}" (get-uri user)))
      (throw (RuntimeException. "could not find lrdd link")))
    (throw (RuntimeException. "could not determine domain"))))

(defn image-link
  [user]
  (or (:avatar-url user)
      (when (:email user) (gravatar-image (:email user)))
      (gravatar-image (get-uri user false))))

(defn vcard-request
  [user]
  (let [body (element/make-element
              "query" {"xmlns" ns/vcard-query})
        packet-map {:from (tigase/make-jid "" (config :domain))
                    :to (tigase/make-jid user)
                    :id "JIKSNU1"
                    :type :get
                    :body body}]
    (tigase/make-packet packet-map)))

(defn count-records
  ([] (count-records {}))
  ([params]
     (mc/count collection-name params)))
