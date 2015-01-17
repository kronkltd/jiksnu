(ns jiksnu.actions.user-actions
  (:require [ciste.config :refer [config]]
            [ciste.core :refer [defaction]]
            [ciste.initializer :refer [definitializer]]
            [ciste.model :as cm]
            [clojure.data.json :as json]
            [clojure.tools.logging :as log]
            [lamina.core :as l]
            [lamina.trace :as trace]
            [jiksnu.actions :refer [invoke-action]]
            [jiksnu.actions.auth-actions :as actions.auth]
            [jiksnu.actions.domain-actions :as actions.domain]
            [jiksnu.actions.key-actions :as actions.key]
            [jiksnu.actions.resource-actions :as actions.resource]
            [jiksnu.actions.webfinger-actions :as actions.webfinger]
            [jiksnu.channels :as ch]
            [jiksnu.model :as model]
            [jiksnu.model.domain :as model.domain]
            [jiksnu.model.key :as model.key]
            [jiksnu.model.user :as model.user]
            [jiksnu.model.webfinger :as model.webfinger]
            [jiksnu.namespace :as ns]
            [jiksnu.ops :as ops]
            [jiksnu.session :as session]
            [jiksnu.templates.actions :as templates.actions]
            [jiksnu.transforms :as transforms]
            [jiksnu.transforms.user-transforms :as transforms.user]
            [jiksnu.util :as util]
            [slingshot.slingshot :refer [throw+]])
  (:import java.net.URI
           jiksnu.model.User))

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
      transforms.user/set-domain
      ;; transforms.user/set-username
      transforms.user/set-_id
      transforms.user/set-local
      ;; transforms.user/set-url
      ;; transforms.user/assert-unique
      ;; transforms.user/set-update-source
      transforms.user/set-discovered
      transforms.user/set-avatar-url

      ;; transforms/set-_id
      transforms/set-updated-time
      transforms/set-created-time
      transforms/set-no-links))

;; utils

(defn get-domain
  "Return the domain of the user"
  [^User user]
  (if-let [domain-name (or (:domain user)
                           (when-let [id (:_id user)]
                             (util/get-domain-name id)))]
    (actions.domain/find-or-create {:_id domain-name})))

(defn get-user-meta-uri
  [user]
  (let [domain (get-domain user)]
    (or (:user-meta-uri user)
        (when-let [id (:_id user)]
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


(defn get-user-meta
  "Returns an enlive document for the user's xrd file"
  [user & [options]]
  (if-let [url (get-user-meta-uri user)]
    (let [response @(ops/update-resource url)]
      (if-let [body (:body response)]
        (cm/string->document body)
        (throw+ "Could not get response")))
    (throw+ "User does not have a meta link")))

;; actions

(defaction add-link*
  [item link]
  ((templates.actions/make-add-link* model.user/collection-name)
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
  (templates.actions/make-indexer 'jiksnu.model.user
                                  :sort-clause {:username 1}))

(defaction index
  [& options]
  (apply index* options))

(defaction profile
  [& _]
  (cm/implement))

(defn parse-xrd
  [body]
  (log/info (.toXML body))
  (let [doc body #_(cm/string->document body)]
    {:links (model.webfinger/get-links doc)}))

(defn process-jrd
  [user jrd & [options]]
  (log/info "processing jrd")
  (let [links (concat (:links user) (:links jrd))]
    (assoc user :links links)))

(defn process-xrd
  [user xrd & [options]]
  (log/info "processing xrd")
  (let [links (concat (:links user) (:links xrd))]
    (assoc user :links links)))

(defn fetch-jrd
  [params & [options]]
  (log/info "fetching jrd")
  (if-let [domain (get-domain params)]
    (if-let [url (model.domain/get-jrd-url domain (:_id params))]
      (if-let [response @(ops/update-resource url options)]
        (when-let [body (:body response)]
          (json/read-str body :key-fn keyword))
        (log/warn "Could not get response"))
      (log/warn "could not determine jrd url"))
    (throw+ "Could not determine domain name")))

(defn fetch-xrd
  [params & [options]]
  (log/info "fetching xrd")
  (if-let [domain (get-domain params)]
    (if-let [url (model.domain/get-xrd-url domain (:_id params))]
      (when-let [xrd (:body @(ops/update-resource url options))]
        (let [doc (cm/string->document xrd)
              username (model.webfinger/get-username-from-xrd doc)]
          (merge params
                 (parse-xrd doc)
                 {:username username})))
      (log/warn "could not determine xrd url"))
    (throw+ "could not determine domain name")))

(defn discover-user-jrd
  [params & [options]]
  (log/info "Discovering user via jrd")
  (if-let [jrd (fetch-jrd params options)]
    (process-jrd params jrd options)
    (do (log/warn "Could not fetch jrd")
        params)))

;; TODO: Collect all changes and update the user once.
(defn discover-user-xrd
  "Retreive user information from webfinger"
  [params & [options]]
  (log/info "Discovering user via xrd")
  (if-let [xrd (fetch-xrd params options)]
    (let [params (process-xrd params xrd options)]
      (merge xrd params))
    (do (log/warn "Could not fetch xrd")
        params)))

(defn get-username-from-http-uri
  [params & [options]]
  ;; HTTP(S) URI
  (log/info "http url")
  (let [id (:_id params)
        uri  (URI. id)]
    (if-let [username (.getUserInfo uri)]
      (do
        (log/debugf "username: %s" username)
        (assoc params :username username))
      (let [params (or (and (:domain params) params)
                       (when-let [domain-name (util/get-domain-name (:_id params))]
                         (assoc params :domain domain-name))
                       (throw+ "Could not determine domain name"))
            params (discover-user-jrd params options)]
        (if (:username params)
          params
          (let [params (discover-user-xrd params options)]
            (if (:username params)
              (let [acct-id (format "acct:%s@%s" (:username params) (:domain params))]
                (merge
                 params
                 {:url id
                  :_id acct-id}))
              (when-let [profile-link (:href (model.user/get-link params "self"))]
                (let [response @(ops/update-resource profile-link {})
                      body (:body response)
                      profile (json/read-str body :key-fn keyword)
                      username (:preferredUsername profile)
                      params (merge params
                                    (when profile
                                      {:username username})
                                    profile)]
                  (if (:username params)
                    params
                    (throw+ "Could not determine username")))))))))))

(defn get-username
  "Given a url, try to determine the username of the owning user"
  [params & [options]]
  (log/info "getting username")
  (let [id (or (:_id params) (:url params))
        uri (URI. id)
        params (assoc params :_id id)]
    (condp = (.getScheme uri)

      "acct"  (do
                (log/debug "acct uri")
                (assoc params :username (first (util/split-uri id))))

      (get-username-from-http-uri params options))))

(defaction find-or-create
  [params & [options]]
  (let [id (:_id params)]
    (or (when id
          (if-let [user (model.user/fetch-by-id id)]
            user
            (do
              (log/debug "user not found by id")
              (if-let [user (let [[uid did] (util/split-uri id)]
                              (model.user/get-user uid did))]
                user
                (do
                  (log/debug "user not found by acct id")
                  (first (model.user/fetch-all {:url id})))))))
        (do
          (log/debug "user not found by url")
          (let [params (if id
                         (get-username params)
                         params)]
            (or (when-let [username (:username params)]
                  (when-let [domain (:domain params)]
                    (model.user/get-user username domain)))
                (create params)))))))

(defaction update
  "Update the user's activities and information."
  [user params]
  (if-let [source-id (:update-source user)]
    (invoke-action "feed-source" "update" (str source-id))
    (log/warn "user does not have an update source"))
  user)

(defn discover-user-meta
  [user & [options]]
  (doseq [params @(l/merge-results
                   (util/safe-task
                    (discover-user-jrd user options))
                   (util/safe-task
                    (discover-user-xrd user options)))]
    (let [links (:links params)]
      (doseq [link links]
        (add-link user link)))))

(defn discover*
  [^User user & [options]]
  (if (:local user)
    (log/info "Local users do not need to be discovered")
    (discover-user-meta user options))
  (model.user/fetch-by-id (:_id user)))

(defaction discover
  "perform a discovery on the user"
  [^User user & [options]]
  @(util/safe-task (discover* user options)))

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
                           :_id (str "acct:" username "@" (config :domain))
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

(defaction show-basic
  [user]
  (show user)
  )

(defaction update-profile
  [options]
  (let [user (session/current-user)]
    ;; TODO: mass assign vulnerability here
    (update user options)))

(defaction xmpp-service-unavailable
  "Error callback when user doesn't support xmpp"
  [user]
  (let [domain-name (:domain user)
        domain (model.domain/fetch-by-id domain-name)]
    @(ops/get-discovered domain)
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

(defaction add-stream
  [user params]
  (let [params (assoc params :user (:_id user))]
    [user @(ops/create-new-stream params)])
  )

;; (definitializer
;;   (model.user/ensure-indexes))
