(ns jiksnu.actions.user-actions
  (:use [ciste.config :only [config]]
        [ciste.core :only [defaction]]
        [ciste.initializer :only [definitializer]]
        [ciste.loader :only [require-namespaces]]
        [clojure.core.incubator :only [-?> -?>>]]
        [jiksnu.actions :only [invoke-action]]
        [slingshot.slingshot :only [throw+]])
  (:require [aleph.http :as http]
            [ciste.model :as cm]
            [clj-statsd :as s]
            [clj-tigase.core :as tigase]
            [clj-tigase.element :as element]
            [clj-tigase.packet :as packet]
            [clojure.string :as string]
            [clojure.data.json :as json]
            [clojure.tools.logging :as log]
            [lamina.core :as l]
            [lamina.trace :as trace]
            [jiksnu.abdera :as abdera]
            [jiksnu.actions.auth-actions :as actions.auth]
            [jiksnu.actions.domain-actions :as actions.domain]
            [jiksnu.actions.key-actions :as actions.key]
            [jiksnu.actions.resource-actions :as actions.resource]
            [jiksnu.actions.webfinger-actions :as actions.webfinger]
            [jiksnu.channels :as ch]
            [jiksnu.helpers.user-helpers :as helpers.user]
            [jiksnu.model :as model]
            [jiksnu.model.domain :as model.domain]
            [jiksnu.model.key :as model.key]
            [jiksnu.model.user :as model.user]
            [jiksnu.model.webfinger :as model.webfinger]
            [jiksnu.namespace :as ns]
            [jiksnu.ops :as ops]
            [jiksnu.session :as session]
            [jiksnu.templates :as templates]
            [jiksnu.transforms :as transforms]
            [jiksnu.transforms.user-transforms :as transforms.user]
            [jiksnu.util :as util]
            [monger.collection :as mc]
            [plaza.rdf.core :as rdf]
            [plaza.rdf.sparql :as sp])
  (:import java.net.URI
           jiksnu.model.User
           org.apache.abdera.model.Person
           tigase.xmpp.JID))

;; hooks

(defonce delete-hooks (ref []))

(defn prepare-delete
  ([item]
     (prepare-delete item @delete-hooks))
  ([item hooks]
     (if (seq hooks)
       (recur ((first hooks) item) (rest hooks))
       item)))

(defn prepare-create
  [user]
  (-> user
      transforms.user/set-id
      transforms.user/set-domain
      transforms.user/set-local
      ;; transforms.user/set-url
      ;; transforms.user/assert-unique
      ;; transforms.user/set-update-source
      transforms.user/set-discovered
      transforms.user/set-avatar-url

      transforms/set-_id
      transforms/set-updated-time
      transforms/set-created-time
      transforms/set-no-links))

;; utils

(defn get-domain
  "Return the domain of the user"
  [^User user]
  (if-let [domain-name (or (:domain user)
                           (when-let [id (:id user)]
                             (util/get-domain-name id)))]
    @(ops/get-domain domain-name)))

(defn get-user-meta-uri
  [user]
  (let [domain (get-domain user)]
    (or (:user-meta-uri user)
        (when-let [id (:id user)]
          (model.domain/get-xrd-url domain id))
        ;; TODO: should update uri in this case
        (model.domain/get-xrd-url domain (:url user)))))

(defn parse-magic-public-key
  [user link]
  (let [key-string (:href link)
        [_ n e] (re-matches
                 #"data:application/magic-public-key,RSA.(.+)\.(.+)"
                 key-string)]
    ;; TODO: this should be calling a key action
    (model.key/set-armored-key (:_id user) n e)))

(defn split-jid
  [^JID jid]
  [(tigase/get-id jid) (tigase/get-domain jid)])


(defn get-user-meta
  "Returns an enlive document for the user's xrd file"
  [user & [options]]
  (if-let [url (get-user-meta-uri user)]
    (let [response @(ops/update-resource url)]
      (if-let [body (:body response)]
        (cm/string->document body)
        (throw+ "Could not get response")))
    (throw+ "User does not have a meta link")))

;; TODO: This is a special case of the discover action for users that
;; support xmpp discovery
(defn request-vcard!
  "Send a vcard request to the xmpp endpoint of the user"
  [user]
  (let [packet (model.user/vcard-request user)]
    (tigase/deliver-packet! packet)))

(defn parse-person
  [^Person person]
  {:id (abdera/get-simple-extension person ns/atom "id")
   :email (.getEmail person)
   :url (str (.getUri person))
   :name (abdera/get-name person)
   :note (abdera/get-note person)
   :username (abdera/get-username person)
   :local-id (-> person
                 (abdera/get-extension-elements ns/statusnet "profile_info")
                 (->> (map #(abdera/attr-val % "local_id")))
                 first)
   :links (abdera/get-links person)})

(defn parse-xrd
  [body]
  (let [doc (cm/string->document body)]
    {:links (model.webfinger/get-links doc)}))

(defn fetch-xrd
  [params & [options]]
  (log/info "fetching xrd")
  (when-let [domain (get-domain params)]
    (when-let [url (model.domain/get-xrd-url domain (:id params))]
      (when-let [response @(ops/update-resource url options)]
        (when-let [body (:body response)]
          (parse-xrd body))))))

(defn fetch-jrd
  [params & [options]]
  (log/info "fetching jrd")
  (when-let [domain (get-domain params)]
    (when-let [url (model.domain/get-jrd-url domain (:id params))]
      (when-let [response @(ops/update-resource url options)]
        (when-let [body (:body response)]
          (json/read-str body))))))

(defn fetch-user-feed
  "returns a feed"
  [^User user & [options]]
  (if-let [url (model.user/feed-link-uri user)]
    (let [response (ops/update-resource url)]
      (abdera/parse-xml-string (:body response)))
    (throw+ "Could not determine url")))

(defaction discover-user-rdf
  "Discover user information from their rdf feeds"
  [user]
  ;; TODO: alternately, check user meta
  (let [uri (:foaf-uri user)
        model (rdf/document-to-model uri :xml)
        query (model.user/foaf-query)]
    (sp/model-query-triples model query)))

(defn fetch-updates-xmpp
  [user]
  ;; TODO: send user timeline request
  (let [packet (tigase/make-packet
                {:to (tigase/make-jid user)
                 :from (tigase/make-jid "" (config :domain))
                 :type :get
                 :body (element/make-element
                        ["pubsub" {"xmlns" ns/pubsub}
                         ["items" {"node" ns/microblog}]])})]
    (tigase/deliver-packet! packet)))

;; actions

(defaction add-link*
  [item link]
  ((templates/make-add-link* model.user/collection-name)
   item link))

(defn add-link
  [user link]
  (if-let [existing-link (model.user/get-link user
                                              (:rel link)
                                              (:type link))]
    user
    (add-link* user link)))

(defaction create
  "create an activity"
  [params]
  (let [links (:links params)
        params (dissoc params :links)
        params (prepare-create params)
        item (model.user/create params)]
    (doseq [link links]
      (add-link item link))
    (model.user/fetch-by-id (:_id item))))

(defaction find-or-create
  [params]
  (let [{:keys [username domain]} params]
    (or (model.user/get-user username domain)
        (create params))))

(defn find-or-create-by-uri
  [uri]
  {:pre [(string? uri)]}
  (let [[username domain] (util/split-uri uri)]
    (find-or-create {:username username
                     :domain domain})))

;; TODO: This is the job of the filter
(defn find-or-create-by-jid
  [^JID jid]
  {:pre [(instance? JID jid)]}
  (let [[username domain] (split-jid jid)]
    (find-or-create {:username username
                     :domain domain})))

(defaction delete
  "Delete the user"
  [^User user]
  ;; {:pre [(instance? User user)]}
  (if-let [user (prepare-delete user)]
    (do (model.user/delete user)
        user)
    (throw+ "prepare delete failed")))

(defaction exists?
  [user]
  ;; {:pre [(instance? User user)]}
  ;; TODO: No need to actually fetch the record
  (model.user/fetch-by-id (:_id user)))

(def index*
  (templates/make-indexer 'jiksnu.model.user
                          :sort-clause {:username 1}))

(defaction index
  [& options]
  (apply index* options))

(defaction profile
  [& _]
  (cm/implement))

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
         ;; TODO: use formatted-uri
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
    (throw+ "Not authorative for this resource")))

(defaction update
  "Update the user's activities and information."
  [user params]
  (if-let [source-id (:update-source user)]
    (invoke-action "feed-source" "update" (str source-id))
    (log/warn "user does not have an update source"))
  user)

(defn process-jrd
  [user jrd & [options]]
  jrd)

(defn process-xrd
  [user xrd & [options]]
  (let [links (concat (:links user) (:links xrd))]
    (assoc user :links links)))

(defn discover-user-jrd
  [user & [options]]
  (log/info "Discovering user via jrd")
  (if-let [jrd (fetch-jrd user options)]
    (process-jrd user jrd options)
    (log/warn "Could not fetch jrd")))

;; TODO: Collect all changes and update the user once.
(defn discover-user-xrd
  "Retreive user information from webfinger"
  [user & [options]]
  (log/info "Discovering user via xrd")
  (if-let [xrd (fetch-xrd user options)]
    (process-xrd user xrd options)
    (log/warn "Could not fetch xrd")))

(defn get-username
  "Given a url, try to determine the username of the owning user"
  [params & [options]]
  (let [id (or (:id params) (:url params))
        uri (URI. id)
        params (assoc params :id id)]
    (condp = (.getScheme uri)

      "acct"  (do
                (log/debug "acct uri")
                (assoc params :username (first (util/split-uri id))))

      ;; HTTP(S) URI
      (do
        (log/debug "http url")
        (if-let [username (.getUserInfo uri)]
          (do
            (log/debugf "username: %s" username)
            (assoc params :username username))
          (if-let [domain-name (or (:domain params) (util/get-domain-name id))]
            (let [params (assoc params :domain domain-name)]
              (or (discover-user-jrd params options)
                  (discover-user-xrd params options)))
            (throw+ "Could not determine domain name")))))))

;; TODO: This function should be called at most once per user, per feed
(defn person->user
  "Extract user information from atom element"
  [^Person person]
  (log/info "converting person to user")
  (trace/trace :person:parsed person)
  (let [{:keys [id username url links note email local-id]
         :as params} (parse-person person)
         domain-name (util/get-domain-name (or id url))
         domain @(ops/get-discovered @(ops/get-domain domain-name))
         username (or username (get-username {:id id}))]
    (if (and username domain)
      (let [user-meta (model.domain/get-xrd-url domain url)
            user (merge params
                        {:domain domain-name
                         :id (or id url)
                         :user-meta-link user-meta
                         :username username})]
        (model/map->User user))
      (throw+ "could not determine user"))))

(defn find-or-create-by-remote-id
  [params & [options]]
  (if-let [id (:id params)]
    (if-let [domain (get-domain params)]
      (let [domain (if (:discovered domain)
                     domain @(ops/get-discovered domain id options))
            params (assoc params :domain (:_id domain))]
        (or (when-let [username (:username params)]
              (model.user/get-user username (:_id domain)))
            (when-let [id (:id params)]
              (model.user/fetch-by-remote-id id))
            (if-let [params (if (:username params)
                              params
                              (get-username params options))]
              (create params)
              (throw+ "could not get username"))))
      (throw+ "could not determine domain"))
    (throw+ "User does not have an id")))

(defn discover-user-meta
  [user & [options]]
  @(util/safe-task
    (discover-user-jrd user options))
  @(util/safe-task
    (let [params (discover-user-xrd user options)
          links (:links params)]
      (doseq [link links]
        (add-link user link)))))

(defn discover*
  [^User user & [options]]
  (if (:local user)
    (log/info "Local users do not need to be discovered")
    (do
      ;; (let [domain (actions.domain/get-discovered (get-domain user))]
      ;;   (when (:xmpp domain)
      ;;     (request-vcard! user)))

      (discover-user-meta user options)

      ;; TODO: there sould be a different discovered flag for
      ;; each aspect of a domain, and this flag shouldn't be set
      ;; till they've all responded
      ;; (model.user/set-field! user :discovered true)
      ))
  (model.user/fetch-by-id (:_id user)))

(defaction discover
  "perform a discovery on the user"
  [^User user & [options]]
  @(util/safe-task (discover* user options)))

;; TODO: xmpp case of update
(defaction fetch-remote
  [user]
  (let [domain (get-domain user)]
    (if (:xmpp domain)
      (request-vcard! user))))

(defaction register
  "Register a new user"
  [{:keys [username password email name location bio] :as options}]
  ;; TODO: should we check reg-enabled here?
  ;; verify submission.
  (if (and username password)
    (if-let [user (model.user/get-user username)]
      (throw+ "user already exists")
      (let [params (merge {:username username
                           :domain (:_id (actions.domain/current-domain))
                           :discovered true
                           :id (str "acct:" username "@" (config :domain))
                           :local true}
                          (when email {:email email})
                          (when name {:name name})
                          (when bio {:bio bio})
                          (when location {:location location}))
            user (create params)]
        (actions.auth/add-password user password)
        (actions.key/generate-key-for-user user)
        user))
    (throw+ "Missing required params")))

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
  (let [user (session/current-user)]
    ;; TODO: mass assign vulnerability here
    (update user options)))

(defaction xmpp-service-unavailable
  "Error callback when user doesn't support xmpp"
  [user]
  (let [domain-name (:domain user)
        domain @(ops/get-discovered @(ops/get-domain domain-name))]
    (actions.domain/set-xmpp domain false)
    user))

(defn subscribe
  [user]
  (if-let [actor-id (session/current-user-id)]
    (do
      (log/infof "Subscribing to %s" (:_id user))
      (ops/create-new-subscription actor-id (:_id user))
      true)
    (throw+ "Must be authenticated")))

(definitializer
  (model.user/ensure-indexes)

  (require-namespaces
   ["jiksnu.filters.user-filters"
    "jiksnu.helpers.user-helpers"
    "jiksnu.sections.user-sections"
    "jiksnu.triggers.user-triggers"
    "jiksnu.views.user-views"]))
