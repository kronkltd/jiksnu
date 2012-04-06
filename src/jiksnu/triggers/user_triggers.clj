(ns jiksnu.triggers.user-triggers
  (:use (ciste [config :only [config]]
               [debug :only [spy]]
               [triggers :only [add-trigger!]])
        (clojure.core [incubator :only [-?>]])
        lamina.core)
  (:require (clj-tigase [core :as tigase]
                        [element :as element])
            (clojure.tools [logging :as log])
            (jiksnu [namespace :as namespace])
            (jiksnu.actions [activity-actions :as actions.activity]
                            [auth-actions :as actions.auth]
                            [stream-actions :as actions.stream]
                            [user-actions :as actions.user])
            (jiksnu.helpers [user-helpers :as helpers.user])
            (jiksnu.model [signature :as model.signature]
                          [user :as model.user])))

(defn discover-trigger
  [action _ user]
  (-?> user :_id
       model.user/fetch-by-id
       actions.stream/load-activities))

(defn fetch-updates-http
  [user]
  (let [uri (helpers.user/feed-link-uri user)]
      (actions.activity/fetch-remote-feed uri)))

(defn fetch-updates-xmpp
  [user]
  ;; TODO: send user timeline request
  (let [packet (tigase/make-packet
                {:to (tigase/make-jid user)
                 :from (tigase/make-jid "" (config :domain))
                 :type :get
                 :body (element/make-element
                        ["pubsub" {"xmlns" namespace/pubsub}
                         ["items" {"node" namespace/microblog}]])})]
    (tigase/deliver-packet! packet)))

(defn fetch-updates-trigger
  [action _ user]
  (let [domain (model.user/get-domain user)]
    (when (:xmpp domain)
      (fetch-updates-xmpp user))
    (fetch-updates-http user)))

(defn create-trigger
  [action params user]
  (actions.user/discover user))

(defn add-link-trigger
  [action [user link] _]
  (condp = (:rel link)
    "magic-public-key" (let [key-string (:href link)
                             [_ n e]
                             (re-matches
                              #"data:application/magic-public-key,RSA.(.+)\.(.+)"
                              key-string)]
                         (model.signature/set-armored-key (:_id user) n e))
    ;; TODO: Fix exstension extraction
    "avatar" (when (= (first (:extensions link)) "96")
               (log/info "setting avatar")
               (actions.user/update (assoc user :avatar-url (:href link))))
    nil))

(defn register-trigger
  [action params user]
  (actions.auth/add-password user (-> params first :password))
  (model.signature/generate-key-for-user user))

(add-trigger! #'actions.user/add-link*     #'add-link-trigger)
(add-trigger! #'actions.user/create        #'create-trigger)
;; (add-trigger! #'actions.user/discover      #'discover-trigger)
(add-trigger! #'actions.user/fetch-updates #'fetch-updates-trigger)
(add-trigger! #'actions.user/register      #'register-trigger)
