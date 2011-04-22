(ns jiksnu.triggers.subscription-triggers
  (:use ciste.core
        ciste.debug
        ciste.triggers
        clj-tigase.core
        jiksnu.actions.subscription-actions
        jiksnu.helpers.subscription-helpers
        jiksnu.namespace
        jiksnu.view)
  (:require [jiksnu.model.domain :as model.domain]
            [jiksnu.model.user :as model.user]))

(defn notify-subscribe-xmpp
  [request subscription]
  (with-serialization :xmpp
    (with-format :xmpp
      (let [user (model.user/fetch-by-id (:from subscription))
            subscribee (model.user/fetch-by-id (:to subscription))
            ele (subscribe-request subscription)
            packet (make-packet {:body (make-element ele)
                                 :type :set
                                 :id (:id request)
                                 :from (make-jid user)
                                 :to (make-jid subscribee)})]
        (.initVars packet)
        (deliver-packet! packet)))))

(defn notify-unsubscribe-xmpp
  [request subscription]
  (with-serialization :xmpp
    (with-format :xmpp
      (let [user (model.user/fetch-by-id (:from subscription))
            subscribee (model.user/fetch-by-id (:to subscription))
            ele (unsubscription-request subscription)
            packet (make-packet {:body (make-element ele)
                                 :type :set
                                 :id (:id request)
                                 :from (make-jid user)
                                 :to (make-jid subscribee)})]
        (.initVars packet)
        (deliver-packet! packet)))))




(defn notify-subscribe
  [action [user] subscription]
  (let [domain (model.domain/show (:domain (spy user)))]
    (if (:xmpp (spy domain))
      (notify-subscribe-xmpp {} subscription)
      ;; TODO: OStatus case
      )))

(defn notify-unsubscribe
  [action [user] subscription]
  (let [domain (model.domain/show (:domain user))]
    (if (:xmpp domain)
      (notify-unsubscribe-xmpp {} subscription)
      ;; TODO: OStatus case
      )))

(add-trigger! #'subscribe #'notify-subscribe)
(add-trigger! #'unsubscribe #'notify-unsubscribe)
