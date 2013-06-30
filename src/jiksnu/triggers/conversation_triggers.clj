(ns jiksnu.triggers.conversation-triggers
  (:require ciste.core
            [clojure.tools.logging :as log]
            [jiksnu.actions.conversation-actions :as actions.conversation]
            [jiksnu.channels :as ch]
            [jiksnu.ops :as ops]
            [lamina.core :as l]))

(defn filter-conversation-create
  [item]
  (#{#'actions.conversation/create} (:action item)))

;; TODO: make op handler
(defn- enqueue-create-local
  [ch]
  (l/enqueue ch (actions.conversation/create {:local true})))

(defn- handle-get-conversation*
  [url]
  (actions.conversation/find-or-create {:url url}))

(def handle-get-conversation
  (ops/op-handler handle-get-conversation*))

(defn init-receivers
  []

  (l/receive-all ch/pending-get-conversation
                 handle-get-conversation)

  (l/receive-all ch/pending-create-conversations
                 enqueue-create-local)

  ;; Create events for each created conversation
  ;; TODO: listen to trace probe
  (l/siphon
   (l/filter* filter-conversation-create (l/fork ciste.core/*actions*))
   ch/posted-conversations))

(defonce receivers (init-receivers))
