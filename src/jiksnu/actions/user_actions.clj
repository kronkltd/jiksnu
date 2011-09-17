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
  (:import com.cliqset.abdera.ext.activity.object.Person
           com.cliqset.magicsig.MagicKey
           com.cliqset.magicsig.MagicSig
           com.cliqset.magicsig.xml.XMLMagicEnvelopeDeserializer
           jiksnu.model.User
           org.apache.commons.codec.binary.Base64
           tigase.xml.Element))

(defonce ^:dynamic *pending-discover-tasks* (ref {}))

(defn pending-domains-key
  [domain]
  (str "pending.domains." domain))

(defn enqueue-discover
  [user]
  (let [domain (:domain user)
        id (:_id user)]
    (redis/sadd (pending-domains-key domain) id)))

(defn pop-user!
  [domain]
  (-?> domain
       pending-domains-key
       redis/spop
       model.user/fetch-by-id))

(declare update)

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
  (let [user (merge {:discovered false :local false} options)
        domain (actions.domain/find-or-create (:domain user))]
    (if (and (:username user)
             (:domain user)
             #_(:id user))
      (model.user/create user))))

(defaction delete
  [id]
  (model.user/delete id))

(defaction discover
  [^User user]
  user)

(defn discover-pending-users
  [domain]
  (if-let [user (pop-user! domain)]
    (do
      (log/info "Discovering: " user)
      (discover user))
    (do (log/info "sleeping")
        (Thread/sleep 3000)))
  (recur domain))

(defaction edit
  [& _])

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

(defn request-vcard!
  [user]
  (let [packet (vcard-request user)]
    (tigase/deliver-packet! packet)))

(defaction fetch-remote
  [user]
  (let [domain (:domain user)]
    (if (:xmpp domain)
      (request-vcard! user))))

(defaction fetch-updates
  [user]
  user)

(declare show)

(defaction find-hub
  [user]
  (let [domain (model.user/get-domain user)]
    domain))

(defaction find-or-create
  [username domain]
  (let [domain-record (actions.domain/find-or-create domain)]
    (if-let [user (model.user/show username domain)]
      user
      (create {:username username
               :domain domain
               :updated (sugar/date)
               :discovered false}))))


(defn user-for-uri
  "Returns a user with the passed account uri,
  or creates one if it does not exist."
  [uri]
  (->> uri model.user/split-uri
      (apply find-or-create)))






(defaction index
  [options]
  (model.user/index))

(defaction profile
  [& _])

(defaction register
  [options]
  (let [{username :username
         password :password
         confirm-password :confirm-password} options]
    (if (and username password confirm-password)
      ;; Passwords must match
      (if (= password confirm-password)
        (let [user {:username username
                    :domain (config :domain)
                    :password password
                    :confirm-password password}]
          (create user))))))

(declare update)

(defaction remote-create
  [user options]
  (let [user (merge user
                    {:updated (sugar/date)
                     :discovered true}
                    options)]
    (update user options)))

(defaction remote-profile
  [& _])

(defaction remote-user
  [user]
  user)

(defaction show
  ;;   "This action just returns the passed user.
  ;; The user needs to be retreived in the filter."
  [user]
  (model.user/fetch-by-id (:_id user)))

(defaction update
  [user params]
  (let [new-params
        (-> (into user
                  (map
                   (fn [[k v]]
                     (if (not= v "")
                       [(keyword k) v]))
                   params))
            (dissoc :id))]
   (model.user/update new-params)))

(defaction update-hub
  [user]
  (if-let [hub-link (-?> user
                         helpers.user/fetch-user-feed
                         abdera/get-hub-link)]
    (do (entity/update User {:_id (:_id user)}
                       {:$set {:hub hub-link}})
        user)))

(defaction xmpp-service-unavailable
  [user]
  (let [domain-name (:domain user)
        domain (actions.domain/find-or-create domain-name)]
    (actions.domain/set-xmpp domain false)
    user))

(defn find-or-create-by-remote-id
  ([user] (find-or-create-by-remote-id user {}))
  ([user params]
     (or (entity/fetch-one User {:id (:id user)} )
         (create (merge user params)))))

(defn person->user
  [^Person person]
  (if (spy person)
    (let [id (.getUri person)
          email (.getEmail person)
          name (.getName person)]
      (find-or-create-by-remote-id
       {:id (str id)}
       (spy (merge {:domain (.getHost id)}
                   (if email {:email email})
                   (if name {:display-name name})))))))
