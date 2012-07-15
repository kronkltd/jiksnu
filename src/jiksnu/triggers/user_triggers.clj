(ns jiksnu.triggers.user-triggers
  (:use [ciste.config :only [config]]
        [ciste.triggers :only [add-trigger!]]
        [clojure.core.incubator :only [-?>]]
        lamina.core
        [slingshot.slingshot :only [throw+]])
  (:require [clj-tigase.core :as tigase]
            [clj-tigase.element :as element]
            [clojure.tools.logging :as log]
            [jiksnu.namespace :as ns]
            [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.actions.auth-actions :as actions.auth]
            [jiksnu.actions.feed-source-actions :as actions.feed-source]
            [jiksnu.actions.key-actions :as actions.key]
            [jiksnu.actions.stream-actions :as actions.stream]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.helpers.user-helpers :as helpers.user]
            [jiksnu.model.key :as model.key]
            [jiksnu.model.user :as model.user]))

(defn fetch-updates-xmpp
  [user]
  ;; TODO: send user timeline request
  (let [packet (tigase/make-packet
                {:to (tigase/make-jid user)
                 :from (tigase/make-jid "" (config :domain))
                 :type :get
                 :body (element/make-element
                        ["pubsub" {"xmlns" ns/pubsub}
                         ["items" {"node" ns/microblog}]])})]
    (tigase/deliver-packet! packet)))

(defn fetch-updates-trigger
  [action _ user]
  (let [domain (model.user/get-domain user)]
    (when (:xmpp domain) (fetch-updates-xmpp user))
    #_(fetch-updates-http user)))

(defn create-trigger
  [action params user]
  (actions.user/discover user))

(defn parse-magic-public-key
  [user link]
  (let [key-string (:href link)
        [_ n e] (re-matches
                 #"data:application/magic-public-key,RSA.(.+)\.(.+)"
                 key-string)]
    (model.key/set-armored-key (:_id user) n e)))

(defn parse-avatar
  [user link]
  (when (= (first (:extensions link)) "96")
    (actions.user/update (assoc user :avatar-url (:href link)))))

(defn parse-updates-from
  [user link]
  (log/debug "Setting update source")
  (if-let [href (:href link)]
    (let [source (actions.feed-source/find-or-create {:topic (:href link)})]
      (model.user/set-field! user :update-source (:_id source)))
    (throw+ "link must have a href")))

(defn add-link-trigger
  [action [user link] _]
  (condp = (:rel link)
    "magic-public-key" (parse-magic-public-key user link)
    "avatar" (parse-avatar user link)
    ns/updates-from (parse-updates-from user link)
    nil))

(defn register-trigger
  [action params user]
  (actions.auth/add-password user (-> params first :password))
  (actions.key/generate-key-for-user user))

(defn discover-trigger
  [action params user]
  (log/info "discover-trigger"))

(add-trigger! #'actions.user/add-link*     #'add-link-trigger)
(add-trigger! #'actions.user/create        #'create-trigger)
(add-trigger! #'actions.user/fetch-updates #'fetch-updates-trigger)
(add-trigger! #'actions.user/register      #'register-trigger)
(add-trigger! #'actions.user/discover      #'discover-trigger)
