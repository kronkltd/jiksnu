(ns jiksnu.transforms.user-transforms
  (:use [ciste.config :only [config]] 
        [clj-gravatar.core :only [gravatar-image]]
        [clojurewerkz.route-one.core :only [named-url]])
  (:require [jiksnu.actions.domain-actions :as actions.domain]))


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
  (if (:avatar-url user)
    user
    (assoc user :avatar-url
           (if (:email user)
             (gravatar-image (:email user))
             (format "http://%s/assets/images/default-avatar.jpg" (config :domain))))))

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

