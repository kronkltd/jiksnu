(ns jiksnu.transforms.user-transforms
  (:require [ciste.config :refer [config]]
            [clj-gravatar.core :refer [gravatar-image]]
            [clojure.tools.logging :as log]
            [jiksnu.actions.domain-actions :as actions.domain]
            [jiksnu.model.domain :as model.domain]
            [jiksnu.model.user :as model.user]
            [jiksnu.model.webfinger :as model.webfinger]
            [jiksnu.routes.helpers :refer [formatted-url]]
            [jiksnu.ops :as ops]
            [jiksnu.util :as util]
            [slingshot.slingshot :refer [throw+]])
  (:import java.net.URI))

(defn set-_id
  [user]
  (if-let [id (:_id user)]
    (let [id-uri (URI. id)]
      (if (= "acct" (.getScheme id-uri))
        user
        (if-let [username (:username user)]
          (if-let [domain-name (:domain user)]
            (let [id (format "acct:%s@%s" username domain-name)]
              (assoc user :_id id))
            (throw+ "could not determine domain"))
          (throw+ "could not determine username"))))
    (if-let [username (:username user)]
      (if-let [domain-name (:domain user)]
        (let [id (format "acct:%s@%s" username domain-name)]
          (assoc user :_id id))
        (throw+ "could not determine domain"))
      (throw+ "could not determine username"))))

(defn set-avatar-url
  [user]
  (if (:avatarUrl user)
    user
    (if-let [avatar-link (first (keep
                                 (fn [link]
                                   (if (= (:rel link) "avatar")
                                     (:href link)))
                                 (:links user)))]
      (assoc user :avatarUrl avatar-link)
      (assoc user :avatarUrl
             (if (:email user)
               (gravatar-image (:email user) :secure? true)
               (format "http://%s/assets/images/default-avatar.jpg" (config :domain)))))))

(defn set-local
  [user]
  (if (contains? user :local)
    user
    (assoc user :local (= (:domain user) (:_id (actions.domain/current-domain))))))

(defn set-discovered
  [user]
  (if (contains? user :discovered)
    user
    (assoc user :discovered false)))

(defn assert-unique
  [user]
  (if-let [id (:_id user)]
    (if-not (model.user/fetch-by-id id)
      user
      (throw+ "already exists"))
    (throw+ "does not have an id")))

(defn set-domain
  [user]
  (or (when-let [domain-name (or (:domain user)
                                 (when-let [id (:_id user)]
                                   (util/get-domain-name id)))]
        (when-let [domain (actions.domain/find-or-create {:_id domain-name})]
          (assoc user :domain domain-name)))
      (throw+ "Could not determine domain for user")))

(defn set-update-source
  [user]
  (if (:local user)
    (let [topic (formatted-url "user timeline" {:id (str (:_id user))} "atom")
          source (ops/get-source topic)]
      (assoc user :update-source (:_id @source)))
    (if (:update-source user)
      user
      (if-let [xrd @(ops/get-user-meta user)]
        (if-let [source (model.webfinger/get-feed-source-from-xrd xrd)]
          (assoc user :update-source (:_id source))
          (throw+ "could not get source"))
        (throw+ "Could not get user meta")))))

(defn set-user-meta-link
  [item]
  (if (:user-meta-link item)
    item
    (let [id (:id item)
          domain-name (:domain item)
          domain (model.domain/fetch-by-id domain-name)]
      (if-let [url (model.domain/get-xrd-url domain id)]
        (assoc item :user-meta-link url)
        (throw+ "Could not determine use meta link")))))
