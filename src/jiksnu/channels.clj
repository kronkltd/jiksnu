(ns jiksnu.channels
  (:require [lamina.core :as l]))

;; async fetchers

(defonce pending-get-conversation
  (l/channel*
   :permanent? true
   :description "pending-get-conversation"))

(defonce pending-get-domain
  (l/channel*
   :permanent? true
   :description "pending-get-domain"))

(defonce pending-get-resource
  (l/channel*
   :permanent? true
   :description "pending-get-resource"))

(defonce pending-get-source
  (l/channel*
   :permanent? true
   :description "pending-get-source"))

(defonce pending-create-conversations
  (l/channel*
   :permanent? true
   :description "pending-create-conversations"))

(defonce pending-update-resources
  (l/channel*
   :permanent? true
   :description "pending-update-resources"))
