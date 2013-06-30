(ns jiksnu.triggers.subscription-triggers
  (:use [ciste.config :only [config]]
        [ciste.core :only [with-context]]
        [ciste.triggers :only [add-trigger!]])
  (:require [clj-tigase.core :as tigase]
            [clj-tigase.element :as element]
            [clojure.tools.logging :as log]
            [jiksnu.actions.subscription-actions :as actions.subscription]
            [jiksnu.channels :as ch]
            [jiksnu.model.user :as model.user]
            [jiksnu.helpers.subscription-helpers :as helpers.subscription]
            [jiksnu.ops :as ops]
            [lamina.core :as l]))

(defn notify-subscribe-xmpp
  [request subscription]
  (with-context [:xmpp :xmpp]
    (let [user (model.user/fetch-by-id (:from subscription))
          subscribee (model.user/fetch-by-id (:to subscription))
          ele (helpers.subscription/subscribe-request subscription)
          packet (tigase/make-packet {:body (element/make-element ele)
                                      :type :set
                                      :id (:id request)
                                      :from (tigase/make-jid user)
                                      :to (tigase/make-jid subscribee)})]
      (tigase/deliver-packet! packet))))

(defn notify-unsubscribe-xmpp
  [request subscription]
  (with-context [:xmpp :xmpp]
    (let [user (model.user/fetch-by-id (:from subscription))
          subscribee (model.user/fetch-by-id (:to subscription))
          ele (helpers.subscription/unsubscription-request subscription)
          packet (tigase/make-packet {:body (element/make-element ele)
                                      :type :set
                                      :id (:id request)
                                      :from (tigase/make-jid user)
                                      :to (tigase/make-jid subscribee)})]
      (tigase/deliver-packet! packet))))

(defn subscribe-trigger
  [action [actor user] subscription]
  (let [domain (model.user/get-domain user)]
    (if (:local user)
      ;; TODO: Verify open subscription
      (actions.subscription/confirm subscription)
      (if (:xmpp domain)
        (notify-subscribe-xmpp {} subscription)
        ;; TODO: OStatus case
        (log/info "sending ostatus subscribe")))))

(defn unsubscribe-trigger
  [action [user] subscription]
  (let [domain (model.user/get-domain user)]
    (if (:xmpp domain)
      (notify-unsubscribe-xmpp {} subscription)
      ;; TODO: OStatus case
      (log/info "sending ostatus unsubscribe"))))

(defn subscribed-trigger
  [action [actor user] subscription]
  (if (model.user/local? user)
    (let [packet (tigase/make-packet
                  {:type :headline
                   :to (tigase/make-jid user)
                   :from (tigase/make-jid "" (config :domain))
                   :body (element/make-element
                          ["body" {}
                           (str (:name actor) " has subscribed to you")])})]
      (tigase/deliver-packet! packet))))

;; (add-trigger! #'actions.subscription/subscribe   #'subscribe-trigger)
;; (add-trigger! #'actions.subscription/unsubscribe #'unsubscribe-trigger)
;; (add-trigger! #'actions.subscription/subscribed  #'subscribed-trigger)

(defn handle-pending-new-subscriptions*
  [actor-id user-id]
  (let [actor (model.user/fetch-by-id actor-id)
        user (model.user/fetch-by-id user-id)]
    (actions.subscription/subscribe actor user)))

(def handle-pending-new-subscriptions
  (ops/op-handler handle-pending-new-subscriptions*))

(defn init-receivers
  []

  (l/receive-all ch/pending-new-subscriptions
                 handle-pending-new-subscriptions)

  )

(defonce receivers (init-receivers))
