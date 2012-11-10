(ns jiksnu.model.user
  (:use [ciste.config :only [config]] 
        [clj-gravatar.core :only [gravatar-image]]
        [clojure.core.incubator :only [-?> -?>>]]
        [clojurewerkz.route-one.core :only [named-url]]
        [jiksnu.model :only [make-id rel-filter map->User]]
        [jiksnu.transforms :only [set-_id set-updated-time set-created-time]]
        [slingshot.slingshot :only [throw+]]
        [validateur.validation :only [acceptance-of validation-set presence-of]])
  (:require [clojure.string :as string]
            [clojure.tools.logging :as log]
            [clj-statsd :as s]
            [clj-tigase.core :as tigase]
            [clj-tigase.element :as element]
            [jiksnu.abdera :as abdera]
            [jiksnu.model :as model]
            [jiksnu.model.domain :as model.domain]
            [jiksnu.namespace :as ns]
            [monger.collection :as mc]
            [monger.query :as mq]
            [plaza.rdf.core :as rdf]
            [plaza.rdf.sparql :as sp])
  (:import java.net.URI
           jiksnu.model.Domain
           jiksnu.model.User
           tigase.xmpp.BareJID
           tigase.xmpp.JID))

(def collection-name "users")
(def default-page-size 20)

(def create-validators
  (validation-set
   (presence-of   :_id)
   (presence-of   :id)
   (acceptance-of :username   :accept string?)
   (acceptance-of :domain     :accept string?)
   (presence-of   :url)
   (presence-of   :created)
   (presence-of   :updated)
   (presence-of   :update-source)
   (presence-of   :avatar-url)
   (acceptance-of :local      :accept (partial instance? Boolean))))

(defn salmon-link
  [user]
  (str "http://" (:domain user) "/main/salmon/user/" (:_id user)))

;; TODO: Move this to actions and make it find-or-create
(defn get-domain
  [^User user]
  (if-let [domain (:domain user)]
    (model.domain/fetch-by-id domain)
    (throw+ (format "User must have a domain field, user = %s" (pr-str user)))))

(defn local?
  [^User user]
  (or (:local user)
      (if-let [domain (get-domain user)]
        (:local domain)
        (throw+ (format "Could not determine domain for user: %s" user)))))

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

(defn get-domain-name
  "Takes a string representing a uri and returns the domain"
  [id]
  (let [uri (URI. id)]
    (if (= "acct" (.getScheme uri))
      (second (split-uri id))
      (.getHost uri))))

(defn display-name
  [^User user]
  (or (:display-name user)
      (when (and (:first-name user) (:last-name user))
        (str (:first-name user) " " (:last-name user)))
      (get-uri user)))

(defn get-link
  [user rel content-type]
  (first (model/rel-filter rel (:links user) content-type)))

(defn drop!
  []
  (mc/remove collection-name))

(defn fetch-by-id
  "Fetch a user by it's object id"
  [id]
  (s/increment "users fetched")
  (if-let [user (mc/find-map-by-id collection-name id)]
    (model/map->User user)
    (log/warnf "Could not find user: %s" id)))

(defn create
  [user]
  (let [errors (create-validators user)]
    (if (empty? errors)
      (do
        (log/debugf "Creating user: %s" user)
        (s/increment "users created")
        (mc/insert collection-name user)
        (fetch-by-id (:_id user)))
      (throw+ {:type :validation
               :errors errors}))))

(defn fetch-all
  ([] (fetch-all {}))
  ([params] (fetch-all params {}))
  ([params options]
     (s/increment "users searched")
     (let [sort-clause (mq/partial-query (mq/sort (:sort-clause options)))
           records (mq/with-collection collection-name
                     (mq/find params)
                     (merge sort-clause)
                     (mq/paginate :page (:page options 1)
                                  :per-page (:page-size options 20)))]
       (map map->User records))))

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
  (log/debugf "setting %s (%s = %s)" (:_id user) field value)
  (s/increment "users field set")
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
                #_{:limit 20})))

;; TODO: Is this needed?
(defn subnodes
  [^BareJID user subnode]
  (let [id (tigase/get-id user)
        domain (tigase/get-domain user)]
    (:nodes (get-user id))))

(defn delete
  "Delete the user"
  [user]
  (s/increment "users deleted")
  (mc/remove-by-id collection-name (:_id user))
  user)

(defn update
  [^User new-user]
  (log/infof "updating user: %s" new-user)
  (let [old-user (get-user (:username new-user) (:domain new-user))
        merged-user (merge {:admin false}
                           old-user new-user)
        user (map->User merged-user)]
    (s/increment "users updated")
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


;; TODO: This should check for an associated source
(defn feed-link-uri
  [^User user]
  (if-let [link (or (get-link user ns/updates-from "application/atom+xml")
                    (get-link user ns/updates-from nil))]
    (:href link)))

(defn fetch-user-feed
  "returns a feed"
  [^User user]
  (-?> user
       feed-link-uri
       abdera/fetch-feed))

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
     (s/increment "users counted")
     (mc/count collection-name params)))

;; FIXME: This does not work yet
(defn foaf-query
  "Extract user information from a foaf document"
  []
  (sp/defquery
    (sp/query-set-vars [:?user :?nick :?name :?bio :?img-url])
    (sp/query-set-type :select)
    (sp/query-set-pattern
     (sp/make-pattern
      [
       [:?uri    rdf/rdf:type                     :foaf/Document]
       [:?uri    :foaf:PrimaryTopic    :?user]
       (rdf/optional [:?user :foaf/nick            :?nick])
       (rdf/optional [:?user :foaf/name            :?name])
       (rdf/optional [:?user :dcterms/descriptions :?bio])
       (rdf/optional [:?user :foaf/depiction       :?img-url])]))))

