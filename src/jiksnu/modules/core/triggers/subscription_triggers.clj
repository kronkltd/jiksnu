(ns jiksnu.modules.core.triggers.subscription-triggers
  (:require [ciste.core :refer [with-context]]
            [clojure.tools.logging :as log]
            [jiksnu.actions.subscription-actions :as actions.subscription]
            [jiksnu.channels :as ch]
            [jiksnu.model.user :as model.user]
            [jiksnu.ops :as ops]
            [manifold.stream :as s]))

(defn handle-pending-new-subscriptions*
  [actor-id user-id]
  (let [actor (model.user/fetch-by-id actor-id)
        user (model.user/fetch-by-id user-id)]
    (actions.subscription/subscribe actor user)))

(def handle-pending-new-subscriptions
  (ops/op-handler handle-pending-new-subscriptions*))

(defn init-receivers
  []
  (s/consume handle-pending-new-subscriptions ch/pending-new-subscriptions))

(defonce receivers (init-receivers))
