(ns jiksnu.actions.user-actions
  (:use (ciste [config :only [config definitializer]]
               [core :only [defaction]]
               [debug :only [spy]]
               [model :only [implement]])
        (clj-stacktrace [repl :only [pst+]])
        (clojure.core [incubator :only [-?> -?>>]])
        (jiksnu model
                [session :only [current-user]])
        plaza.rdf.core
        plaza.rdf.sparql
        plaza.rdf.vocabularies.foaf
        )
  (:require (aleph [http :as http])
            (ciste [model :as cm])
            (clj-tigase [core :as tigase]
                        [element :as element]
                        [packet :as packet])
            (clojure [string :as string])
            (clojure.tools [logging :as log])
            (jiksnu [abdera :as abdera]
                    [namespace :as namespace])
            (jiksnu.actions [domain-actions :as actions.domain])
            (jiksnu.helpers [user-helpers :as helpers.user])
            (jiksnu.model [domain :as model.domain]
                          [signature :as model.signature]
                          [user :as model.user]
                          [webfinger :as model.webfinger])
            (jiksnu.xmpp [element :as xmpp.element])
            (karras [entity :as entity]
                    [sugar :as sugar])
            (plaza.rdf [core :as rdf])
            )
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


(defn get-username
  [id]
  (let [uri (URI. id)]
    (if (= "acct" (.getScheme uri))
      (first (model.user/split-uri id))
      (or (.getUserInfo uri)
          (if-let [domain-name (get-domain-name id)]
            (let [domain (model.domain/fetch-by-id domain-name)]
              (if-let [url (actions.domain/get-user-meta-url domain id)]
                (-?>> (model.webfinger/fetch-host-meta url)
                     model.webfinger/get-identifiers
                     (map #(first (model.user/split-uri %)))
                     (filter identity)
                     first)
                (throw (RuntimeException. "Could not get user meta url"))))
            (throw (RuntimeException. "Could not determine domain name")))))))

(defn get-domain
  [^User user]
  (if-let [domain-id (or (:domain user)
                         (get-domain-name (:id user)))]
    (actions.domain/find-or-create domain-id)))

(defaction add-link*
  [user link]
  (entity/update User {:_id (:_id user)}
                 {:$addToSet {:links link}})
  user)

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
                     :updated (sugar/date)}
                    (when-not (:id options) {:id (model.user/get-uri options)})
                    options)
        ;; This has the side effect of ensuring that the domain is
        ;; created. This should probably be explicitly done elsewhere.
        domain (get-domain user)]
    (model.user/create user)))

(defaction delete
  [^User user]
  (model.user/delete (:_id user)))

(defaction exists?
  [user]
  (model.user/find-record user))

(defn fetch-by-jid
  [jid]
  (model.user/get-user (.getLocalpart jid) (.getDomain jid)))

(defaction index
  [options]
  (model.user/fetch-all {} :sort [(sugar/asc :username)]
                        :limit 20))

(defn local-index
  []
  [])

(defaction profile
  [& _]
  (cm/implement))

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
                                      domain (actions.domain/discover domain)
                                      )]
           (or (model.user/fetch-by-remote-id id)
               (create (merge user
                              {:domain (:_id domain)}
                              (if-let [username (or (:username user)
                                                    (:username params))]
                                nil
                                {:username (get-username id)})
                              params)))
           (throw (RuntimeException. "domain has not been disovered")))
         (throw (RuntimeException. "could not determine domain")))
       (throw (RuntimeException. "User does not have an id")))))

(defn find-or-create-by-uri
  [uri]
  (apply find-or-create (model.user/split-uri uri)))

(defn update-hub*
  [user feed]
  (when-let [hub-link (abdera/get-hub-link feed)]
    (model.user/set-field user :hub hub-link)
    user))

(defaction update-hub
  "Determine the user's hub link and update the user object"
  [user]
  (if-let [feed (helpers.user/fetch-user-feed user)]
    (update-hub* user feed)))

(defaction user-meta
  [uri]
  (->> uri
       model.user/split-uri
       (apply model.user/get-user)))

(defn request-vcard!
  [user]
  (let [packet (model.user/vcard-request user)]
    (tigase/deliver-packet! packet)))

(defaction update
  [user params]
  (->> params
       (map (fn [[k v]] (if (not= v "") [(keyword k) v])))
       (into user)
       model.user/update))

(defn person->user
  [^Person person]
  (if person
    (let [id (str (.getUri person))
          domain-name (get-domain-name id)
          domain (actions.domain/find-or-create domain-name)
          email (.getEmail person)
          name (or (.getSimpleExtension person namespace/poco
                                        "displayName" "poco" )
                   (.getName person))
          username (or (.getSimpleExtension person namespace/poco
                                            "preferredUsername" "poco")
                       (get-username id)
                       )
          note (.getSimpleExtension person (QName. namespace/poco "note"))
          uri (str (.getUri person))
          links (-> person
                    (.getExtensions (QName. namespace/atom "link"))
                    (->> (map abdera/parse-link)))
          params (merge {:domain domain-name}
                        (when uri {:uri uri})
                        (when username {:username username})
                        (when note {:bio note})
                        (when email {:email email})
                        (when name {:display-name name}))]
      (if username
        (let [user (-> {:id id}
                       #_(find-or-create-by-remote-id params)
                       (merge params))]
          (doseq [link links]
            (add-link user link))
          (entity/make User user))
        (throw (RuntimeException. "could not determine user"))
        ))))


;; TODO: Collect all changes and update the user once.
(defaction update-usermeta
  [user]
  (if-let [xrd (helpers.user/fetch-user-meta (spy user))]
    (let [links (model.webfinger/get-links (spy xrd))
          new-user (assoc user :links links)
          feed (helpers.user/fetch-user-feed new-user)
          user (merge user
                      (-?>
                       (or (-?> feed .getAuthor)
                           (-?> feed .getEntries first .getAuthor))
                       person->user))
          avatar-url (-?> feed (.getLinks "avatar") seq first .getHref str)]
      (update-hub* user feed)
      (doseq [link links]
        (add-link user link))
      (-> user
          (merge (when avatar-url {:avatar-url avatar-url}))
          update))))

(defn foaf-query
  []
  (defquery
    (query-set-vars [:?user :?nick :?name :?bio :?img-url])
    (query-set-type :select)
    (query-set-pattern
     (make-pattern
      [
       [:?uri    rdf/rdf:type                     :foaf/Document]
       [:?uri    :foaf:PrimaryTopic    :?user]
       (optional [:?user :foaf/nick            :?nick])
       (optional [:?user :foaf/name            :?name])
       (optional [:?user :dcterms/descriptions :?bio])
       (optional [:?user :foaf/depiction       :?img-url])
       ]))))


(defaction discover-user-rdf
  [user]
  (let [uri (:foaf-uri user)
        model (document-to-model uri :xml)
        query (foaf-query)]
    (model-query-triples model query)))

(defaction discover-user-xmpp
  [user]
  (log/info "discover xmpp")
  (request-vcard! user))

(defaction discover-user-http
  [user]
  (log/info "discovering http")
  (update-usermeta user))

(defaction discover
  [^User user]
  (when user
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
            (model.user/set-field user :discovered true))
          (do
            ;; Domain not yet discovered
            (actions.domain/discover domain)

            ;; TODO: need to recurse here, but not forever unless the
            ;; domain is bogus. Perhaps a try counter.
            #_(enqueue-discover user)))))))

;; TODO: turn this into a worker
(defn discover-pending-users
  [domain]
  #_(if-let [user (pop-user! domain)]
      (do
        (log/info "Discovering: " user)
        (discover user))
      (do (log/info "sleeping")
          #_(Thread/sleep 3000)))
  #_(recur domain))

(defaction fetch-remote
  [user]
  (let [domain (get-domain user)]
    (if (:xmpp domain)
      (request-vcard! user))))

(defaction find-hub
  [user]
  (implement
      (get-domain user)))

(defaction register
  [{:keys [username password email display-name location bio] :as options}]

  ;; verify submission.
  (if (:accepted options)
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
            ;; TODO: make a trigger
            ;; (actions.auth/add-password user password)

            ;; return the created user
            user)
          (throw (IllegalArgumentException. "user already exists"))))
      (throw (IllegalArgumentException. "Missing required params")))
    (throw (IllegalArgumentException. "you didn't check the box"))))

(defaction register-page
  "Display the form to reqister a user"
  []
  ;; init an empty user model?
  (User.))

;; deprecated, nothing should hit this in the future. If anything is,
;; I want it drug out into the street and shot
(defaction remote-create
  [user options]
  (let [user (merge user
                    {:updated (sugar/date)
                     :discovered true}
                    options)]
    (create user options)))

(defaction show
  "This action just returns the passed user.
   The user needs to be retreived in the filter."
  [user]
  (model.user/fetch-by-id (:_id user)))

(defaction update-profile
  [options]
  (let [user (current-user)]
    ;; TODO: mass assign vulnerability here
    (update user options)))

;; TODO: this applies only for acct: uris
(defn user-for-uri
  "Returns a user with the passed account uri,
   or creates one if it does not exist."
  [uri]
  (->> uri model.user/split-uri
       (apply find-or-create)))

(defaction xmpp-service-unavailable
  [user]
  (let [domain-name (:domain user)
        domain (actions.domain/find-or-create domain-name)]
    (actions.domain/set-xmpp domain false)
    user))

(defn get-user-meta-uri
  [user]
  (let [domain (get-domain user)]
    (or (:user-meta-uri user)
        (actions.domain/get-user-meta-url domain (:id user)))))

(definitializer
  (doseq [namespace ['jiksnu.filters.user-filters
                     'jiksnu.helpers.user-helpers
                     'jiksnu.sections.user-sections
                     'jiksnu.triggers.user-triggers
                     'jiksnu.views.user-views
                     ]]
    (require namespace)))
