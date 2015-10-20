(ns jiksnu.modules.web.filters.pubsub-filters
  (:require [ciste.filters :refer [deffilter]]
            [taoensso.timbre :as log]
            [jiksnu.actions.pubsub-actions :as actions.pubsub]))

(deffilter #'actions.pubsub/hub-dispatch :http
  [action request]
  (let [params (:params request)
        event
        {:mode          (get params "hub.mode")
         :callback      (get params "hub.callback")
         :challenge     (get params "hub.challenge")
         :lease-seconds (get params "hub.lease_seconds")
         :verify        (get params "hub.verify")
         :verify-token  (get params "hub.verify_token")
         :secret        (get params "hub.secret")
         :topic         (get params "hub.topic")}]
    (action event)))
