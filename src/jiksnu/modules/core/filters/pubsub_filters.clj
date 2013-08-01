(ns jiksnu.filters.pubsub-filters
  (:use [ciste.filters :only [deffilter]]
        [jiksnu.actions.pubsub-actions :only [hub-dispatch]])
  (:require [clojure.tools.logging :as log]))

(deffilter #'hub-dispatch :http
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
