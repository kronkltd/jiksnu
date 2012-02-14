(println "reading user actions")
(ns jiksnu.actions.user-actions
  (:use (ciste [config :only [config definitializer]]
               [core :only [defaction]]
               [debug :only [spy]])
        (clj-stacktrace [repl :only [pst+]])
        (clojure.core [incubator :only [-?> -?>>]])
        (jiksnu model
                [session :only [current-user]]))
  (:require (aleph [http :as http])
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
                    [sugar :as sugar]))
  (:import javax.xml.namespace.QName
           jiksnu.model.User
           org.apache.abdera2.model.Person
           org.apache.commons.codec.binary.Base64
           tigase.xml.Element
           tigase.xmpp.JID))

(defn get-domain
  [^User user]
  (-> user :domain actions.domain/find-or-create))

(defaction add-link*
  [user link]
  (entity/update User {:_id (:_id user)}
                 {:$addToSet {:links link}})
  user)

(defn add-link
  [user link]
  (if-let [existing-link (model.user/get-link user (:rel link))]
    user
    (add-link* user link)))

(defaction create
  [options]
  (let [user (merge {:discovered false
                     :local false
                     :updated (sugar/date)}
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
  (model.user/fetch-all {} :sort [(sugar/asc :username)]))

(defaction profile
  [& _])

(defaction fetch-updates
  [user]
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
     (or (model.user/fetch-by-remote-id (:id user))
         (create (merge user params)))))

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
      (let [user (-> {:id (str id)}
                     #_(find-or-create-by-remote-id params)
                     (merge params))]
        #_(doseq [link links]
            (add-link user link))
        (entity/make User user)))))




;; TODO: Collect all changes and update the user once.
(defaction update-usermeta
  [user]
  (if-let [xrd (helpers.user/fetch-user-meta user)]
    (let [links (model.webfinger/get-links xrd)
          new-user (assoc user :links links)
          feed (helpers.user/fetch-user-feed new-user)
          user (-?>> feed .getAuthor person->user (merge user))
          avatar-url (-?> feed (.getLinks "avatar") seq first .getHref str)]
      (update-hub* user feed)
      (doseq [link links]
        (add-link user link))
      (-> user
          (merge (when avatar-url {:avatar-url avatar-url}))
          update))))


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
    (when (not (:local user))
      (let [domain (model.user/get-domain user)]
        (if (:discovered domain)
          (do (discover-user-xmpp user)
              (discover-user-http user))
          #_(enqueue-discover user))))
    (model.user/set-field user :discovered true)
    user))

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
  (let [domain (:domain user)]
    (if (:xmpp domain)
      (request-vcard! user))))

(defaction find-hub
  [user]
  (get-domain user))

(defaction register
  [{:keys [username password email display-name location]}]
  (if (and username password)
    (-> {:username username
         :domain (config :domain)
         :discovered true
         :id (str "acct:" username "@" (config :domain))
         :local true
         ;; TODO: encrypt here
         :password password}
        (merge (when email {:email email})
               (when display-name {:display-name display-name})
               (when location {:location location}))
        create)
    (throw (IllegalArgumentException. "Missing required params"))))

(defaction register-page
  []
  true)

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
  (let [domain (get-domain user)]
    (or (:user-meta-uri user)
        (actions.domain/get-user-meta-url domain (:id user)))))

(definitializer
  (println "running user action initializer")
  (doseq [namespace ['jiksnu.filters.user-filters
                     'jiksnu.helpers.user-helpers
                     'jiksnu.sections.user-sections
                     'jiksnu.triggers.user-triggers
                     'jiksnu.views.user-views
                     ]]
    (require namespace)))
