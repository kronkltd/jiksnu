(ns jiksnu.transforms.user-transforms
  (:use [ciste.config :only [config]]
        [clj-gravatar.core :only [gravatar-image]]
        [clojurewerkz.route-one.core :only [named-url]]
        [jiksnu.routes.helpers :only [formatted-url]]
        [slingshot.slingshot :only [throw+]])
  (:require [ciste.model :as cm]
            [clojure.tools.logging :as log]
            [jiksnu.actions.domain-actions :as actions.domain]
            [jiksnu.model.domain :as model.domain]
            [jiksnu.model.user :as model.user]
            [jiksnu.model.webfinger :as model.webfinger]
            [jiksnu.ops :as ops]
            [jiksnu.util :as util]))

(defn set-id
  [user]
  (if (:id user)
    user
    (assoc user :id (format "acct:%s@%s" (:username user) (:domain user)))))

(defn set-url
  [user]
  (if (:url user)
    user
    (assoc user :url (named-url "local user timeline" user))))

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
               (gravatar-image (:email user))
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
  (if-let [id (:id user)]
    (if-not (model.user/fetch-by-remote-id id)
      user
      (throw+ "already exists"))
    (throw+ "does not have an id")))

(defn set-domain
  [user]
  (if (:domain user)
    user
    (if-let [id (:id user)]
      (let [domain-name (util/get-domain-name id)]
        (assoc user :domain domain-name))
      (throw+ "Could not determine domain for user"))))

(defn set-update-source
  [user]
  (if (:local user)
    (let [topic (formatted-url "user timeline" {:id (str (:_id user))} "atom")
          source (ops/get-source topic)]
      (assoc user :update-source (:_id @source)))
    (if (:update-source user)
      user
      (if-let [xrd @(ops/get-user-meta user)]
        (if-let [source (model.webfinger/get-feed-source-from-xrd @xrd)]
          (assoc user :update-source (:_id source))
          (throw+ "could not get source"))
        (throw+ "Could not get user meta")))))

(defn set-user-meta-link
  [item]
  (if (:user-meta-link item)
    item
    (let [id (:id item)
          domain-name (:domain item)
          domain (ops/get-discovered (model.domain/fetch-by-id domain-name))]
      (if-let [url (model.domain/get-xrd-url @domain id)]
        (assoc item :user-meta-link url)
        (throw+ "Could not determine use meta link")))))

