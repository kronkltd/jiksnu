(ns jiksnu.modules.xmpp.filters.subscription-filters
  (:use [ciste.filters :only [deffilter]]
        [jiksnu.actions.subscription-actions :only [confirm delete get-subscribers
                                                    get-subscriptions index ostatus ostatussub
                                                    ostatussub-submit remote-subscribe-confirm
                                                    show subscribe subscribed unsubscribe]]
        [jiksnu.session :only [current-user current-user-id]]
        [slingshot.slingshot :only [throw+]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.model :as model]
            [jiksnu.model.subscription :as model.subscription]
            [jiksnu.model.user :as model.user])
  (:import tigase.xmpp.JID))

(deffilter #'get-subscribers :xmpp
  [action request]
  (when-let [item (model.user/fetch-by-jid (:to request))]
    (action item)))

(deffilter #'get-subscriptions :xmpp
  [action request]
  (when-let [item (model.user/fetch-by-jid (:to request))]
    (action item)))

;; remote-subscribe-confirm

(deffilter #'remote-subscribe-confirm :xmpp
  [action {:keys [^JID to ^JID from] :as request}]
  (let [subscriber (model.user/fetch-by-jid to)
        subscribee (model.user/fetch-by-jid from)]
    (when-let [subscription (model.subscription/fetch-all
                             {:to (:_id subscribee) :from (:_id subscriber)})]
      (action subscription))))

(deffilter #'subscribe :xmpp
  [action {:keys [^JID to ^JID from] :as request}]
  (when-let [user (model.user/fetch-by-jid to)]
    (action user)))

;; subscribed

(deffilter #'subscribed :xmpp
  [action {:keys [^JID to ^JID from] :as request}]
  (when-let [subscriber (model.user/fetch-by-jid from)]
    (when-let [subscribee (model.user/fetch-by-jid to)]
      (action subscriber subscribee))))

(deffilter #'unsubscribe :xmpp
  [action {:keys [^JID to ^JID from] :as request}]
  (when-let [user (model.user/fetch-by-jid to)]
    (when-let [subscriber (actions.user/find-or-create-by-jid from)]
      (action (:_id subscriber) (:_id user)))))
