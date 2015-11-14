(ns jiksnu.actions.user-actions
  (:require [ciste.config :refer [config]]
            [ciste.initializer :refer [definitializer]]
            [ciste.model :as cm]
            [clojure.data.json :as json]
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
            [slingshot.slingshot :refer [throw+]]
            [taoensso.timbre :as timbre])
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

(defn add-link*
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

(defn create
  "create an activity"
  [params]
  (let [links (:links params)
        params (dissoc params :links)
        params (prepare-create params)
        item (model.user/create params)]
    (doseq [link links]
      (add-link item link))
    (model.user/fetch-by-id (:_id item))))

(defn delete
  "Delete the user"
  [^User user]
  ;; {:pre [(instance? User user)]}
  (if-let [user (prepare-delete user)]
    (do (model.user/delete user)
        user)
    (throw+ "prepare delete failed")))

(defn exists?
  [user]
  ;; {:pre [(instance? User user)]}
  ;; TODO: No need to actually fetch the record
  (model.user/fetch-by-id (:_id user)))

(def index*
  (templates.actions/make-indexer 'jiksnu.model.user
                                  :sort-clause {:username 1}))

(defn index
  [& options]
  (apply index* options))

(defn parse-xrd
  [body]
  (timbre/info (.toXML body))
  (let [doc body #_(cm/string->document body)]
    {:links (model.webfinger/get-links doc)}))

(defn process-xrd
  [user xrd & [options]]
  (timbre/info "processing xrd")
  (let [links (concat (:links user) (:links xrd))]
    (assoc user :links links)))

(defn fetch-xrd
  [params & [options]]
  (timbre/info "fetching xrd")
  (if-let [domain (get-domain params)]
    (do (util/inspect params)
        (if-let [url (model.domain/get-xrd-url domain (:_id params))]
          (when-let [xrd (:body @(ops/update-resource url options))]
            (let [doc (cm/string->document xrd)
                  username (model.webfinger/get-username-from-xrd doc)]
              (merge params
                     (parse-xrd doc)
                     {:username username})))
          (timbre/warn "could not determine xrd url")))
    (throw+ "could not determine domain name")))

;; TODO: Collect all changes and update the user once.
(defn discover-user-xrd
  "Attempt to retreive User information from webfinger

  If information can not be found, return params"
  [params & [options]]
  (timbre/info "Discovering User via XRD")
  (if-let [xrd (fetch-xrd params options)]
    (let [params (process-xrd params xrd options)]
      (merge xrd params))
    (do (timbre/warn "Could not fetch XRD")
        params)))

(defn get-username-from-http-uri
  [{id :_id username :username :as params} & [options]]
  (timbre/info "HTTP(S) URI")
  (util/inspect id)
  (if-let [username (some-> id URI. .getUserInfo)]
    (do
      (timbre/debugf "Username from uri: %s" username)
      (assoc params :username username))
    (do
      (timbre/debug "Could not determine username from uri")
      (if-let [domain (or (:domain params) (util/get-domain-name id))]
        (let [params (assoc params :domain params)]
          (if (:username params)
            params
            (let [params (discover-user-xrd params options)]
              (if (:username params)
                (do
                  (timbre/debug "Swapping user id for url")
                  (let [acct-id (format "acct:%s@%s" (:username params) (:domain params))]
                    (merge params {:url id :_id acct-id})))
                (do
                  (timbre/debug "Does not have a username from xrd")
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
                        (throw+ "Could not determine username")))))))))))))

(defn get-username
  "Given a url, try to determine the username of the owning user"
  [params & [options]]
  (timbre/info "getting username")
  (let [id (or (:_id params) (:url params))
        uri (URI. id)
        params (assoc params :_id id)]
    (condp = (.getScheme uri)

      "acct"  (do
                (timbre/debug "acct uri")
                (assoc params :username (first (util/split-uri id))))

      (get-username-from-http-uri params options))))

(defn find-or-create
  [{id :_id
    :keys [username domain]
    :as params} & [options]]
  (let [id (:_id params)]
    (or (when id
          (or (model.user/fetch-by-id id)
              (do
                (timbre/debug "user not found by id")
                (or (let [[uid did] (util/split-uri id)]
                      (model.user/get-user uid did))
                    (do
                      (timbre/debug "user not found by acct id")
                      (first (model.user/fetch-all {:url id})))))))
        (do
          (timbre/debug "user not found by url")
          (let [params (if id
                         (get-username params)
                         params)]
            (or (when (and username domain)
                  (model.user/get-user username domain))
                (create params)))))))

(defn update-record
  "Update the user's activities and information."
  [^User user params]
  (if-let [source-id (:update-source user)]
    (invoke-action "feed-source" "update" (str source-id))
    (timbre/warn "user does not have an update source"))
  user)

(defn discover-user-meta
  ([user]
   (discover-user-meta user nil))
  ([^User user options]
   (doseq [params (discover-user-xrd user options)]
     (let [links (:links params)]
       (doseq [link links]
         (add-link user link))))))

(defn discover*
  [^User user & [options]]
  (if (:local user)
    (timbre/info "Local users do not need to be discovered")
    (discover-user-meta user options))
  (model.user/fetch-by-id (:_id user)))

(defn discover
  "perform a discovery on the user"
  [^User user & [options]]
  (discover* user options))

(defn register
  "Register a new user"
  [{:keys [username password email name location bio] :as options}]
  ;; TODO: should we check reg-enabled here?
  ;; verify submission.
  (if (and username password)
    (if-let [user (model.user/get-user username)]
      (throw+ {:type :conflict
               :msg "user already exists"
               :username username})
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
    (throw+ {:type :missing-param
             :msg "Missing required params"})))

(defn show
  "This action just returns the passed user.
   The user needs to be retreived in the filter."
  [user]
  user)

(defn show-basic
  [user]
  (show user))

(defn update-profile
  [options]
  (let [user (session/current-user)]
    ;; TODO: mass assign vulnerability here
    (update-record user options)))

(defn subscribe
  [user]
  (if-let [actor-id (session/current-user-id)]
    (do
      (timbre/infof "Subscribing to %s" (:_id user))
      (ops/create-new-subscription actor-id (:_id user))
      true)
    (throw+ {:type :auth
             :msg "Must be authenticated"})))

(defn add-stream
  [user params]
  (let [params (assoc params :user (:_id user))]
    [user @(ops/create-new-stream params)]))

;; (definitializer
;;   (model.user/ensure-indexes))
