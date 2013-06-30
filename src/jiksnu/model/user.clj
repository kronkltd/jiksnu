(ns jiksnu.model.user
  (:use [ciste.config :only [config]]
        [clj-gravatar.core :only [gravatar-image]]
        [clojure.core.incubator :only [-?> -?>>]]
        [clojurewerkz.route-one.core :only [named-url]]
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
            [jiksnu.templates :as templates]
            [jiksnu.util :as util]
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
(def maker model/map->User)

(def create-validators
  (validation-set
   (presence-of   :_id)
   (acceptance-of :id           :accept string?)
   (acceptance-of :username     :accept string?)
   (acceptance-of :domain       :accept string?)
   (acceptance-of :url          :accept string?)
   (presence-of   :created)
   (presence-of   :updated)
   (presence-of   :update-source)
   (presence-of   :avatarUrl)
   (acceptance-of :local         :accept (partial instance? Boolean))))

(def count-records (templates/make-counter     collection-name))
(def delete        (templates/make-deleter     collection-name))
(def drop!         (templates/make-dropper     collection-name))
(def set-field!    (templates/make-set-field!  collection-name))
(def fetch-by-id   (templates/make-fetch-by-id collection-name maker))
(def create        (templates/make-create      collection-name #'fetch-by-id #'create-validators))
(def fetch-all     (templates/make-fetch-fn    collection-name maker))

(defn salmon-link
  [user]
  (named-url "user salmon" {:id (:_id user)}))

(defn get-uri
  ([^User user] (get-uri user true))
  ([^User user use-scheme?]
     (str (when use-scheme? "acct:") (:username user) "@" (:domain user))))

(defn image-link
  [user]
  (or (:avatarUrl user)
      (when (:email user) (gravatar-image (:email user)))
      (gravatar-image (get-uri user false))))

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

(defn display-name
  [^User user]
  (or (:name user)
      (when (and (:first-name user) (:last-name user))
        (str (:first-name user) " " (:last-name user)))
      (get-uri user)))

(defn get-link
  [user rel content-type]
  (first (util/rel-filter rel (:links user) content-type)))

(defn get-user
  "Find a user by username and domain"
  ([username] (get-user username (config :domain)))
  ([username domain]
     (if-let [user (mc/find-one-as-map collection-name
                                       {:username username
                                        :domain domain})]
       (maker user))))

;; deprecated
;; TODO: Split the jid into it's parts and fetch.
(defn fetch-by-jid
  [jid]
  (get-user (.getLocalpart jid)
            (.getDomain jid)))

(defn fetch-by-uri
  "Fetch user by their acct uri"
  [uri]
  (let [[username domain-name] (util/split-uri uri)]
    (when (and username domain-name)
      (get-user username domain-name))))

(defn fetch-by-remote-id
  "Fetch user by their id value"
  [uri]
  (if-let [user (mc/find-one-as-map collection-name {:id uri})]
    (maker user)))

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

(defn update
  [^User new-user]
  (log/infof "updating user: %s" new-user)
  (let [old-user (get-user (:username new-user) (:domain new-user))
        merged-user (merge {:admin false}
                           old-user new-user)
        user (maker merged-user)]
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
      (throw+ "could not find lrdd link"))
    (throw+ "could not determine domain")))

;; TODO: This should check for an associated source
(defn feed-link-uri
  [^User user]
  (if-let [link (or (get-link user ns/updates-from "application/atom+xml")
                    (get-link user ns/updates-from nil))]
    (:href link)))

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
