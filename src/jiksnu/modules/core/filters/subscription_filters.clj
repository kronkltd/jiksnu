(ns jiksnu.modules.core.filters.subscription-filters
  (:use [ciste.filters :only [deffilter]]
        [clojure.core.incubator :only [-?> -?>>]]
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
            [jiksnu.model.user :as model.user]
            [jiksnu.util :as util])
  (:import tigase.xmpp.JID))

;; delete

(deffilter #'delete :command
  [action id]
  (when-let [item (model.subscription/fetch-by-id id)]
    (action item)))

(deffilter #'get-subscribers :page
  [action request]
  (let [item (:item request)]
    (action item)))

(deffilter #'get-subscriptions :page
  [action request]
  (let [item (:item request)]
    (action item)))

;; index

(deffilter #'index :page
  [action request]
  (action))

