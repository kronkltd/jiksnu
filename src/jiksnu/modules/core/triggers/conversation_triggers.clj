(ns jiksnu.modules.core.triggers.conversation-triggers
  (:require ciste.core
            [clojure.tools.logging :as log]
            [jiksnu.actions.conversation-actions :as actions.conversation]
            [jiksnu.channels :as ch]
            [jiksnu.ops :as ops]
            [manifold.stream :as s]))

(defn filter-conversation-create
  [item]
  (#{#'actions.conversation/create} (:action item)))

;; TODO: make op handler
(defn- enqueue-create-local
  [ch]
  (s/put! ch (actions.conversation/create {:local true})))

(defn- handle-get-conversation*
  [url]
  (actions.conversation/find-or-create {:url url}))

(def handle-get-conversation
  (ops/op-handler handle-get-conversation*))

(defn init-receivers
  []
  (s/consume handle-get-conversation ch/pending-get-conversation)
  (s/consume enqueue-create-local ch/pending-create-conversations)

  ;; Create events for each created conversation
  ;; TODO: listen to trace probe
  #_(s/connect
     (s/filter filter-conversation-create ciste.core/*actions*)
     ch/posted-conversations))

(defonce receivers (init-receivers))
