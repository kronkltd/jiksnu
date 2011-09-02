(ns jiksnu.actions.user-actions
  (:use (ciste config core debug)
        (jiksnu model namespace
                [session :only (current-user)]
                view)
        jiksnu.helpers.user-helpers
        jiksnu.xmpp.element)
  (:require (aleph [http :as http])
            (clj-tigase [core :as tigase])
            [clojure.string :as string]
            [clojure.tools.logging :as log]
            (jiksnu [abdera :as abdera]
                    [redis :as redis])
            (jiksnu.actions [domain-actions :as actions.domain]
                            [webfinger-actions :as actions.webfinger])
            (jiksnu.model [domain :as model.domain]
                          [signature :as model.signature]
                          [user :as model.user])
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

(defn enqueue-discover
  [user]
  (let [domain (:domain user)
        id (:_id user)]
    (redis/sadd (str "pending.domains." domain) id)))

(defn pop-user!
  [domain]
  (dosync
   (if-let [s (get @*pending-discover-tasks* domain)]
     (if-let [f (first s)]
       (do (alter *pending-discover-tasks*
                  #(assoc %
                     domain
                     (disj s f)))
           f)))))

(declare update)

(defaction add-link
  [user link]
  (if-let [existing-link (model.user/get-link user (:rel link))]
    user
    (entity/update
     User {:_id (:_id user)}
     {:$addToSet
      {:links {:href (:href link)
               :type (:type link)
               :rel (:rel link)}}})))

(defaction create
  [options]
  (let [user (merge {:discovered false :local false} options)
        domain (actions.domain/find-or-create (:domain user))]
    (if (and (:username user)
             (:domain user)
             (:id user))
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
      (apply find-or-create) :_id))






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
  (let [feed (fetch-user-feed user)
        hub-link (abdera/get-hub-link feed)]
    (entity/update
     User {:_id (:_id user)}
     {:$set {:hub hub-link}})
    user))

;; TODO: Collect all changes and update the user once.
(defaction update-usermeta
  [user]
  (let [xrd (fetch-user-meta user)
        links (actions.webfinger/get-links xrd)
        new-user (assoc user :links links)
        feed (fetch-user-feed new-user)
        author (.getAuthor feed)
        uri (.getUri author)]
    (doseq [link links]
      (if (= (:rel link) "magic-public-key")
        (let [key-string (:href link)
              [_ n e]
              (re-matches
               #"data:application/magic-public-key,RSA.(.+)\.(.+)"
               key-string)]
          (model.signature/set-armored-key (:_id user) n e)))
      (add-link user link))
    (update
     (-> user
         (assoc :remote-id (str uri))
         (assoc :discovered true)))))

(defaction xmpp-service-unavailable
  [user]
  (let [domain-name (:domain user)
        domain (model.domain/show domain-name)]
    (actions.domain/set-xmpp domain false)))

(defn find-or-create-by-remote-id
  ([user] (find-or-create-by-remote-id user {}))
  ([user params]
     (or (entity/fetch-one User {:id (:id user)} )
         (create (merge user params)))))

(defn person->user
  [^Person person]
  (let [id (.getUri person)
        email (.getEmail person)
        name (.getName person)]
    (find-or-create-by-remote-id
     {:id (str id)}
     (merge {:domain (.getHost id)}
            (if email {:email email})
            (if name {:display-name name})))))
