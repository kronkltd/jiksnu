(ns jiksnu.actions.user-actions
  (:use (ciste [config :only (config)]
               [core :only (defaction)]
               [debug :only (spy)])
        (clojure.contrib [core :only (-?>)])
        (jiksnu model
                [session :only (current-user)]))
  (:require (aleph [http :as http])
            (clj-tigase [core :as tigase]
                        [element :as element]
                        [packet :as packet])
            (clojure [string :as string])
            (clojure.tools [logging :as log])
            (jiksnu [abdera :as abdera]
                    [namespace :as namespace]
                    [redis :as redis]
                    [view :as view])
            (jiksnu.actions [domain-actions :as actions.domain])
            (jiksnu.helpers [user-helpers :as helpers.user])
            (jiksnu.model [domain :as model.domain]
                          [signature :as model.signature]
                          [user :as model.user])
            (jiksnu.xmpp [element :as xmpp.element])
            (karras [entity :as entity]
                    [sugar :as sugar]))
  (:import jiksnu.model.User
           org.apache.commons.codec.binary.Base64
           tigase.xml.Element
           tigase.xmpp.JID))

(defonce ^:dynamic *pending-discover-tasks* (ref {}))

(defn enqueue-discover
  [user]
  (let [domain (:domain user)
        id (:_id user)]
    (redis/sadd (model.domain/pending-domains-key domain) id)))

(defn pop-user!
  [domain]
  (-?> domain
       model.domain/pending-domains-key
       redis/spop
       model.user/fetch-by-id))



(defaction add-link
  [user link]
  (if-let [existing-link (model.user/get-link user (:rel link))]
    user
    (do (entity/update
         User {:_id (:_id user)}
         {:$addToSet
          {:links {:href (:href link)
                   :type (:type link)
                   :rel (:rel link)}}})
        user)))

(defaction create
  [options]
  (let [user (-> options
                 (assoc :discovered false)
                 (assoc :local false)
                 (assoc :updated (sugar/date)))]
    (-> user :domain actions.domain/find-or-create)
    (model.user/create user)))

(defaction delete
  [id]
  (model.user/delete id))

(defaction discover
  [^User user]
  user)

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

(defaction edit
  [& _])

(defn fetch-by-id
  [id]
  (model.user/fetch-by-id id))

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
  (model.user/get-domain user))

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

(defn person->user
  [person]
  (if person
    (let [id (.getUri person)
          email (.getEmail person)
          name (or (.getSimpleExtension person namespace/poco
                                        "displayName" "poco" )
                   (.getName person))
          username (.getSimpleExtension person namespace/poco
                                        "preferredUsername" "poco")
          params (merge {:domain (.getHost id)}
                        (if username {:username username})
                        (if email {:email email})
                        (if name {:display-name name}))]
      (find-or-create-by-remote-id
       {:id (str id)} params))))

(defaction profile
  [& _])

(defaction register
  [username password]
  (create {:username username
           :domain (config :domain)
           ;; TODO: encrypt here
           :password password}))

(defaction register-page
  []
  true)

(declare vcard-request)

(defn request-vcard!
  [user]
  (let [packet (vcard-request user)]
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
  (-> user
      (into (map
             (fn [[k v]] (if (not= v "") [(keyword k) v]))
             params))
      model.user/update))

(defaction update-hub
  [user]
  (if-let [hub-link (-?> user
                         helpers.user/fetch-user-feed
                         abdera/get-hub-link)]
    (do (entity/update User {:_id (:_id user)}
                       {:$set {:hub hub-link}})
        user)))

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

(defn vcard-request
  [user]
  (let [body (element/make-element
              "query" {"xmlns" namespace/vcard-query})
        packet-map {:from (tigase/make-jid "" (config :domain))
                    :to (tigase/make-jid user)
                    :id "JIKSNU1"
                    :type :get
                    :body body}]
    (tigase/make-packet packet-map)))

(defaction xmpp-service-unavailable
  [user]
  (let [domain-name (:domain user)
        domain (actions.domain/find-or-create domain-name)]
    (actions.domain/set-xmpp domain false)
    user))
