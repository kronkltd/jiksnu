(ns jiksnu.modules.xmpp.actions.comment-actions
  (:use [ciste.config :only [config]]
        [ciste.core :only [defaction]])
  (:require [ciste.model :as cm]
            [clj-tigase.core :as tigase]
            [clj-tigase.element :as element]
            [clojure.tools.logging :as log]
            [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.user :as model.user]
            [jiksnu.namespace :as ns]
            [lamina.trace :as trace]))

(defn comment-request
  [activity]
  (tigase/make-packet
   {:type :get
    :from (tigase/make-jid "" (config :domain))
    :to (tigase/make-jid (model.activity/get-author activity))
    :body
    (element/make-element
     ["pubsub" {"xmlns" ns/pubsub}
      ["items" {"node" (comment-node-uri activity)}]])}))

(defn fetch-comments-onesocialweb
  [activity]
  (cm/implement))

;; This should be a trigger
(defaction fetch-comments-remote
  [activity]
  (let [author (model.activity/get-author activity)
        domain (model.user/get-domain author)]
    (when (:xmpp domain)
      (tigase/deliver-packet! (comment-request activity)))))

