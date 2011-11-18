(ns jiksnu.actions.user-actions
  (:use (ciste [config :only [config definitializer]]
               [core :only [defaction]]
               [debug :only [spy]])
        clj-stacktrace.repl
        (clojure.core [incubator :only [-?>]])
        (jiksnu model
                [session :only [current-user]]))
  (:require (aleph [http :as http])
            (clj-tigase [core :as tigase]
                        [element :as element]
                        [packet :as packet])
            (clojure [string :as string])
            (clojure.tools [logging :as log])
            (jiksnu [abdera :as abdera]
                    [namespace :as namespace]
                    [redis :as redis])
            (jiksnu.actions [domain-actions :as actions.domain])
            (jiksnu.helpers [user-helpers :as helpers.user])
            (jiksnu.model [domain :as model.domain]
                          [signature :as model.signature]
                          [user :as model.user]
                          [webfinger :as model.webfinger])
            (jiksnu.xmpp [element :as xmpp.element])
            (karras [entity :as entity]
                    [sugar :as sugar]))
  (:import javax.xml.namespace.QName
           jiksnu.model.User
           org.apache.abdera2.model.Person
           org.apache.commons.codec.binary.Base64
           tigase.xml.Element
           tigase.xmpp.JID))

(defonce ^:dynamic *pending-discover-tasks* (ref {}))

(defn enqueue-discover
  "Queues the user to be discoverd once discovery of the domain has been completed."
  [user]
  (let [domain (:domain user)
        id (:_id user)]
    1
    #_(redis/sadd (model.domain/pending-domains-key domain) id)))

(defn pop-user!
  [domain]
  (-?> domain
       model.domain/pending-domains-key
       redis/spop
       model.user/fetch-by-id))

(defn get-domain
  [^User user]
  (-> user :domain actions.domain/find-or-create))

(defaction add-link
  [user link]
  (if-let [existing-link (model.user/get-link user (:rel link))]
    user
    (do (entity/update User {:_id (:_id user)}
                       {:$addToSet {:links link}})
        user)))

(defaction create
  [options]
  (let [user (merge {:discovered false
                     :local false
                     :updated (sugar/date)}
                    options)]
    (-> user :domain actions.domain/find-or-create)
    (model.user/create user)))

(defaction delete
  [id]
  (model.user/delete id))

(defaction discover
  [^User user]
  (when (not (:local user))
    user))

;; TODO: turn this into a worker
(defn discover-pending-users
  [domain]
  (if-let [user (pop-user! domain)]
    (do
      (log/info "Discovering: " user)
      (discover user))
    (do (log/info "sleeping")
        #_(Thread/sleep 3000)))
  #_(recur domain))

(defn fetch-by-jid
  [jid]
  (model.user/show (.getLocalpart jid) (.getDomain jid)))

(defn fetch-by-uri
  [uri]
  (model.user/fetch-by-uri uri))

(declare request-vcard!)

(defaction fetch-remote
  [user]
  (let [domain (:domain user)]
    (if (:xmpp domain)
      (request-vcard! user))))

(defaction fetch-updates
  [user]
  user)

(defaction find-hub
  [user]
  (get-domain user))

(defaction find-or-create
  [username domain]
  (or (model.user/show username domain)
      (create {:username username :domain domain})))

(defn find-or-create-by-jid
  [^JID jid]
  (find-or-create (tigase/get-id jid) (tigase/get-domain jid)))

(defn find-or-create-by-remote-id
  ([user] (find-or-create-by-remote-id user {}))
  ([user params]
     (or (entity/fetch-one User {:id (:id user)} )
         (create (merge user params)))))

(defn find-or-create-by-uri
  [uri]
  (apply find-or-create (model.user/split-uri uri)))

(defaction index
  [options]
  (model.user/index))

(defaction profile
  [& _])

(defaction register
  [{:keys [username password email display-name location]}]
  (if (and username password)
    (-> {:username username
         :domain (config :domain)
         :discovered true
         :local true
         ;; TODO: encrypt here
         :password password}
        (merge (when email {:email email})
               (when display-name {:display-name display-name})
               (when location {:location location}))
        create)))

(defaction register-page
  []
  true)

(declare vcard-request)

(defn request-vcard!
  [user]
  (let [packet (model.user/vcard-request user)]
    (tigase/deliver-packet! packet)))

(defaction remote-create
  [user options]
  (let [user (merge user
                    {:updated (sugar/date)
                     :discovered true}
                    options)]
    (create user options)))

(defaction show
  ;;   "This action just returns the passed user.
  ;; The user needs to be retreived in the filter."
  [user]
  (model.user/fetch-by-id (:_id user)))

(defaction update
  [user params]
  (->> params
       (map (fn [[k v]] (if (not= v "") [(keyword k) v])))
       (into user)
       model.user/update))

(defn person->user
  [^Person person]
  (if person
    (let [id (.getUri person)
          email (.getEmail person)
          name (or (.getSimpleExtension person namespace/poco
                                        "displayName" "poco" )
                   (.getName person))
          username (.getSimpleExtension person namespace/poco
                                        "preferredUsername" "poco")
          links (-> person
                    (.getExtensions (QName. namespace/atom "link"))
                    (->> (map abdera/parse-link)))
          params (merge {:domain (.getHost id)}
                        (when username {:username username})
                        (when email {:email email})
                        (when name {:display-name name}))]
      (let [user (update (merge (find-or-create-by-remote-id
                                 {:id (str id)} params)
                                params))]
        (doseq [link links]
          (add-link user link))
        user))))

(defn update-hub*
  [user feed]
  (if-let [hub-link (abdera/get-hub-link feed)]
    (do (entity/update User {:_id (:_id user)}
                       {:$set {:hub hub-link}})
        user)))

(defaction update-hub
  ;; Determine the user's hub link and update the user object
  [user]
  (if-let [feed (helpers.user/fetch-user-feed user)]
    (update-hub* user feed)))

(defaction update-profile
  [options]
  (let [user (current-user)]
    (update user options)))

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
  (let [username (:username user)
        domain (get-domain user)]
    (or (:user-meta-uri user)
        (actions.domain/get-user-meta-uri (:_id domain) username))))

(defn fetch-user-meta
  "returns a user meta document"
  [^User user]
  (-?> user
      model.user/user-meta-uri
      model.webfinger/fetch-host-meta))

;; TODO: Collect all changes and update the user once.
(defaction update-usermeta
  [user]
  (let [xrd (fetch-user-meta user)
        links (model.webfinger/get-links xrd)
        new-user (assoc user :links links)
        feed (helpers.user/fetch-user-feed new-user)
        author (when feed (.getAuthor feed))
        user (merge user (person->user author))
        avatar-url (-?> feed (.getLinks "avatar") seq first .getHref str)
        uri (:uri user)]
    (update-hub* user feed)
    (doseq [link links]
      (add-link user link))
    (-> (merge
         user
         (when avatar-url {:avatar-url avatar-url})
         {:id (str uri)
          :discovered true})
        update)))

(defaction user-meta
  [uri]
  (->> uri
       model.user/split-uri
       (apply model.user/show )))

(definitializer
  (doseq [namespace ['jiksnu.filters.user-filters
                     'jiksnu.helpers.user-helpers
                     'jiksnu.sections.user-sections
                     'jiksnu.triggers.user-triggers
                     'jiksnu.views.user-views
                     ]]
    (require namespace)))
