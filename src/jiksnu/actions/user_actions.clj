(ns jiksnu.actions.user-actions
  (:use [ciste.config :only [config]]
        [ciste.core :only [defaction]]
        [ciste.initializer :only [definitializer]]
        [ciste.model :only [implement]]
        [ciste.loader :only [require-namespaces]]
        [clojure.core.incubator :only [-?> -?>>]]
        [jiksnu.session :only [current-user]]
        [slingshot.slingshot :only [throw+]])
  (:require [aleph.http :as http]
            [ciste.model :as cm]
            [clj-tigase.core :as tigase]
            [clj-tigase.element :as element]
            [clj-tigase.packet :as packet]
            [clj-time.core :as time]
            [clojure.string :as string]
            [clojure.tools.logging :as log]
            [jiksnu.abdera :as abdera]
            [jiksnu.actions.auth-actions :as actions.auth]
            [jiksnu.actions.domain-actions :as actions.domain]
            [jiksnu.helpers.user-helpers :as helpers.user]
            [jiksnu.model :as model]
            [jiksnu.model.domain :as model.domain]
            [jiksnu.model.key :as model.key]
            [jiksnu.model.user :as model.user]
            [jiksnu.model.webfinger :as model.webfinger]
            [jiksnu.namespace :as ns]
            [jiksnu.xmpp.element :as xmpp.element]
            [monger.collection :as mc]
            [plaza.rdf.core :as rdf]
            [plaza.rdf.sparql :as sp])
  (:import java.net.URI
           javax.xml.namespace.QName
           jiksnu.model.User
           org.apache.abdera2.model.Person
           org.apache.commons.codec.binary.Base64
           tigase.xml.Element
           tigase.xmpp.JID))

(defn get-domain-name
  "Takes a string representing a uri and returns the domain"
  [id]
  (let [uri (URI. id)]
    (if (= "acct" (.getScheme uri))
      (second (model.user/split-uri id))
      (.getHost uri))))


(defn get-username-from-atom-property
  [user-meta]
  (try
    (->> user-meta
         (cm/query "//*[local-name() = 'Property'][@type = 'http://apinamespace.org/atom/username']")
         model/force-coll
         (keep #(.getValue %))
         first)
    ;; TODO: What are the error risks here?
    (catch RuntimeException ex
      (log/error "caught error" ex)
      (.printStackTrace ex))))

(defn get-username-from-identifiers
  [user-meta]
  (try
    (->> user-meta
         model.webfinger/get-identifiers
         (keep (comp first model.user/split-uri))
         first)
    (catch RuntimeException ex
      (log/error "caught error" ex)
      (.printStackTrace ex))))

;; takes a document
(defn get-username-from-user-meta
  "return the username component of the user meta"
  [user-meta]
  (->> [(get-username-from-atom-property user-meta)]
       (lazy-cat
        [(get-username-from-identifiers user-meta)])
       (filter identity)
       first))

(defn get-username
  "Given a url, try to determine the username of the owning user"
  [id]
  (let [uri (URI. id)]
    (if (= "acct" (.getScheme uri))
      (first (model.user/split-uri id))
      (or (.getUserInfo uri)
          (if-let [domain-name (get-domain-name id)]
            (let [domain (model.domain/fetch-by-id domain-name)]
              (or
               ;; Try getting the username from user-meta
               (-?>> (actions.domain/get-user-meta-url domain id)
                     model.webfinger/fetch-host-meta
                     get-username-from-user-meta)))
            (throw (RuntimeException. "Could not determine domain name")))))))

(defn get-domain
  "Return the domain of the user"
  [^User user]
  (if-let [domain-id (or (:domain user)
                         (get-domain-name (:id user)))]
    (actions.domain/find-or-create {:_id domain-id})))

(defn get-user-meta-uri
  [user]
  (let [domain (get-domain user)]
    (or (:user-meta-uri user)
        (actions.domain/get-user-meta-url domain (:id user)))))

(defaction add-link*
  [user link]
  (mc/update "users" {:_id (:_id user)}
             {:$addToSet {:links link}})
  user)

;; FIXME: this is always hitting the else branch
(defn add-link
  [user link]
  (if-let [existing-link (model.user/get-link user
                                              (:rel link)
                                              (:type link))]
    user
    (add-link* user link)))

(defaction create
  [options]
  (let [user (merge {:discovered false
                     :local false
                     :updated (time/now)}
                    (when-not (:id options) {:id (model.user/get-uri options)})
                    options)]
    ;; This has the side effect of ensuring that the domain is
    ;; created. This should probably be explicitly done elsewhere.
    (if-let [domain (get-domain user)]
      (model.user/create user)
      (throw (RuntimeException. "Could not determine domain for user")))))

(defaction delete
  "Delete the user"
  [^User user]
  (model.user/delete (:_id user)))

(defaction exists?
  [user]
  ;; TODO: No need to actually fetch the record
  (model.user/fetch-by-id (:_id user)))

;; DEPRICATED
(defn fetch-by-jid
  [jid]
  (model.user/get-user (.getLocalpart jid) (.getDomain jid)))

(defaction index
  "List all of the users"
  [options]
  (let [page (get options :page 1)
        page-size 20
        criteria {:sort [{:username 1}]
                  :limit 20}
        record-count (model.user/count-records {})
        records (model.user/fetch-all {} criteria)]
    {:items records
     :page page
     :page-size page-size
     :total-records record-count
     :args options}))

;; (defn local-index
;;   []
;;   (implement))

(defaction profile
  [& _]
  (implement))

(defaction fetch-updates
  [user]
  ;; TODO: stream action?
  user)

(defaction find-or-create
  [username domain]
  (or (model.user/get-user username domain)
      (create {:username username :domain domain})))

(defn find-or-create-by-jid
  [^JID jid]
  (find-or-create (tigase/get-id jid) (tigase/get-domain jid)))

(defn find-or-create-by-remote-id
  ([user] (find-or-create-by-remote-id user {}))
  ([user params]
     (if-let [id (:id user)]
       (if-let [domain (get-domain user)]
         (if-let [discovered-domain (if (:discovered domain)
                                      domain (actions.domain/discover domain id))]
           (or (model.user/fetch-by-remote-id id)
               (if-let [username (or (:username user) (get-username id))]
                 (create (merge user
                                {:domain (:_id domain)
                                 :username username}))
                 (log/warn (str "Could not determine username for: " id))))
           (throw (RuntimeException. "domain has not been disovered")))
         (throw (RuntimeException. "could not determine domain")))
       (throw (RuntimeException. "User does not have an id")))))

(defn find-or-create-by-uri
  [uri]
  (apply find-or-create (model.user/split-uri uri)))

(defaction user-meta
  "returns a user matching the uri"
  [user]
  (if (model.user/local? user)
    (let [full-uri (model.user/full-uri user)]
      {:subject (model.user/get-uri user)
       :alias full-uri
       :links
       [
        {:rel ns/wf-profile 
         :type "text/html"
         :href full-uri}
        
        {:rel ns/hcard
         :type "text/html"
         :href full-uri}

        {:rel ns/xfn 
         :type "text/html"
         :href full-uri}

        {:rel ns/updates-from
         :type "application/atom+xml"
         :href (str "http://" (config :domain) "/api/statuses/user_timeline/" (:_id user) ".atom")}

        {:rel ns/updates-from
         :type "application/json"
         :href (str "http://" (config :domain) "/api/statuses/user_timeline/" (:_id user) ".json")}

        {:rel "describedby"
         :type "application/rdf+xml"
         :href (str full-uri ".rdf")}

        {:rel "salmon"          :href (model.user/salmon-link user)}
        {:rel ns/salmon-replies :href (model.user/salmon-link user)}
        {:rel ns/salmon-mention :href (model.user/salmon-link user)}
        {:rel ns/oid-provider   :href full-uri}
        {:rel ns/osw-service    :href (str "xmpp:" (:username user) "@" (:domain user))}
        

        {:rel "magic-public-key"
         :href (-> user
                   model.key/get-key-for-user
                   model.key/magic-key-string)}

        {:rel ns/ostatus-subscribe 
         :template (str "http://" (config :domain) "/main/ostatussub?profile={uri}")}


        {:rel ns/twitter-username 
         :href (str "http://" (config :domain) "/api/")
         :property [{:type "http://apinamespace.org/twitter/username"
                     :value (:username user)}]}]})
    (throw (RuntimeException. "Not authorative for this resource"))))

(defn request-vcard!
  "Send a vcard request to the xmpp endpoint of the user"
  [user]
  (let [packet (model.user/vcard-request user)]
    (tigase/deliver-packet! packet)))

(defaction update
  "Update fields in the user"
  [user params]
  ;; TODO: injection attack
  (->> params
       (map (fn [[k v]] (if (not= v "") [(keyword k) v])))
       (into user)
       model.user/update))

(defn get-name
  "Returns the name of the Atom person"
  [^Person person]
  (or (.getSimpleExtension person ns/poco "displayName" "poco" )
      (.getName person)))


;; TODO: This function should be called at most once per user, per feed
(defn person->user
  "Extract user information from atom element"
  [^Person person]
  (let [id (str (.getUri person))
        ;; TODO: check for custom domain field first?
        domain-name (get-domain-name id)
        domain (actions.domain/find-or-create {:_id domain-name})
        username (or (.getSimpleExtension person ns/poco
                                          "preferredUsername" "poco")
                     (get-username id))]
    (if (and username domain)
      (let [email (.getEmail person)
            name (get-name person)
            note (.getSimpleExtension person (QName. ns/poco "note"))
            uri (str (.getUri person))
            ;; homepage 
            local-id (-> person
                         (.getExtensions (QName. ns/statusnet "profile_info"))
                         (->> (map (fn [extension]
                                     (.getAttributeValue extension "local_id")
                                     )))
                         first
                         )
            links (-> person
                      (.getExtensions (QName. ns/atom "link"))
                      (->> (map abdera/parse-link)))
            params (merge {:domain domain-name}
                          (when uri {:uri uri})
                          (when username {:username username})
                          (when note {:bio note})
                          (when email {:email email})
                          (when local-id {:local-id local-id})
                          (when name {:display-name name}))
            user (-> {:id id}
                     #_(find-or-create-by-remote-id params)
                     (merge params))]
        (doseq [link links]
          (add-link user link))
        (model/map->User user))
      (throw+ "could not determine user"))))


;; TODO: Collect all changes and update the user once.
(defaction update-usermeta
  "Retreive user information from webfinger"
  [user]
  ;; TODO: This is doing way more than it's supposed to
  (if-let [xrd (helpers.user/fetch-user-meta user)]
    (let [links (model.webfinger/get-links xrd)
          new-user (assoc user :links links)
          feed (helpers.user/fetch-user-feed new-user)
          first-entry (-?> feed .getEntries first)
          user (merge user
                      (-?> (abdera/get-author first-entry feed)
                           person->user))
          avatar-url (-?> feed (.getLinks "avatar") seq first .getHref str)]
      (if (seq links)
        (doseq [link links]
          (add-link user link))
        (log/warn "usermeta has no links"))
      (model.user/set-field! user :avatar-url avatar-url))
    (throw+ "Could not fetch user-meta")))

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

(defaction discover-user-rdf
  "Discover user information from their rdf feeds"
  [user]
  ;; TODO: alternately, check user meta
  (let [uri (:foaf-uri user)
        model (rdf/document-to-model uri :xml)
        query (foaf-query)]
    (sp/model-query-triples model query)))

(defaction discover-user-xmpp
  [user]
  (log/info "discover xmpp")
  (request-vcard! user))

(defaction discover-user-http
  [user]
  (log/info "discovering http")
  (update-usermeta user))

(defaction discover
  "perform a discovery on the user"
  [^User user & [options & _]]
  (future (loop [try-count (get options :try-count 1)]
     (when (< try-count 5)
       (if (:local user)
         user
         ;; Get domain should, in theory, always return a domain, or else error
         (let [domain (get-domain user)]
           (if (:discovered domain)
             (do
               (when (:xmpp domain) (discover-user-xmpp user))
               ;; There should be a similar check here so we're not
               ;; hitting xmpp-only services.
               ;; This is really OStatus specific
               (discover-user-http user)
               ;; TODO: there sould be a different discovered flag for
               ;; each aspect of a domain, and this flag shouldn't be set
               ;; till they've all responded
               (model.user/set-field! user :discovered true)
               (model.user/fetch-by-id (:_id user)))
             (do
               ;; Domain not yet discovered
               (actions.domain/discover domain)
               (recur (inc try-count)))))))))
  user)

(defaction fetch-remote
  [user]
  (let [domain (get-domain user)]
    (if (:xmpp domain)
      (request-vcard! user))))

(defaction register
  "Register a new user"
  [{:keys [username password email display-name location bio] :as options}]
  ;; TODO: should we check reg-enabled here?
  ;; verify submission.
  (if (and username password)
    (let [user (model.user/get-user username)]
      (if-not user
        (let [user (-> {:username username
                        :domain (config :domain)
                        :discovered true
                        :id (str "acct:" username "@" (config :domain))
                        :local true}
                       (merge (when email {:email email})
                              (when display-name {:display-name display-name})
                              (when bio {:bio bio})
                              (when location {:location location}))
                       create)]
          (actions.auth/add-password user password)
          user)
        (throw (IllegalArgumentException. "user already exists"))))
    (throw (IllegalArgumentException. "Missing required params"))))

(defaction register-page
  "Display the form to reqister a user"
  []
  (model/->User))

(defaction show
  "This action just returns the passed user.
   The user needs to be retreived in the filter."
  [user]
  user)

(defaction update-profile
  [options]
  (let [user (current-user)]
    ;; TODO: mass assign vulnerability here
    (update user options)))

;; TODO: this applies only for acct: uris
;; TODO: use find-or-create
(defn user-for-uri
  "Returns a user with the passed account uri,
   or creates one if it does not exist."
  [uri]
  (->> uri model.user/split-uri
       (apply find-or-create)))

(defaction xmpp-service-unavailable
  "Error callback when user doesn't support xmpp"
  [user]
  (let [domain-name (:domain user)
        domain (actions.domain/find-or-create domain-name)]
    (actions.domain/set-xmpp domain false)
    user))

(definitializer
  (require-namespaces
   ["jiksnu.filters.user-filters"
    "jiksnu.helpers.user-helpers"
    "jiksnu.sections.user-sections"
    "jiksnu.triggers.user-triggers"
    "jiksnu.views.user-views"])

 ;; cascade delete on domain deletion
  (dosync
   (alter actions.domain/delete-hooks
          conj (fn [domain]
                 (doseq [user (model.user/fetch-by-domain domain)]
                   (delete user))))))
