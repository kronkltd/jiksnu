(ns jiksnu.modules.core.triggers.subscription-triggers
  (:use [ciste.config :only [config]]
        [ciste.core :only [with-context]])
  (:require [clj-tigase.element :as element]
            [clojure.tools.logging :as log]
            [jiksnu.actions.subscription-actions :as actions.subscription]
            [jiksnu.channels :as ch]
            [jiksnu.model.user :as model.user]
            [jiksnu.ops :as ops]
            [lamina.core :as l]))

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
