(ns jiksnu.actions.conversation-actions
  (:use [ciste.initializer :only [definitializer]]
        [ciste.core :only [defaction]]
        [ciste.loader :only [require-namespaces]]
        [clojure.core.incubator :only [-?>>]]
        [jiksnu.transforms :only [set-_id set-updated-time set-created-time]])
  (:require [clj-statsd :as s]
            [clojure.tools.logging :as log]
            [jiksnu.actions.feed-source-actions :as actions.feed-source]
            [jiksnu.model :as model]
            [jiksnu.model.conversation :as model.conversation]
            [jiksnu.model.feed-source :as model.feed-source]
            [jiksnu.transforms :as transforms]
            [jiksnu.transforms.conversation-transforms :as transforms.conversation]
            [lamina.core :as l]))

(defn prepare-create
  [conversation]
  (-> conversation
      transforms/set-_id
      transforms.conversation/set-local
      transforms.conversation/set-update-source
      transforms/set-updated-time
      transforms/set-created-time))

(defaction create
  [params]
  (let [conversation (prepare-create params)]
    (s/increment "conversation_created")
    (model.conversation/create conversation)))

(defaction delete
  [conversation]
  (model.conversation/delete conversation))

(def index*
  (model/make-indexer 'jiksnu.model.conversation))

(defaction index
  [& [params & [options]]]
  (index* params options))

(defaction find-or-create
  [options]
  (if-let [conversation (or (if-let [id (:_id options)] (first (model.conversation/fetch-by-id id)))
                            (if-let [url (:url options)] (first (model.conversation/find-by-url url))))]
    conversation
    (create options)))

(defaction show
  [record]
  record)

(definitializer
  (l/receive-all
   model/pending-conversations
   (fn [[url ch]]
     (l/enqueue ch (find-or-create {:url url}))))

  (require-namespaces
   ["jiksnu.filters.conversation-filters"
    "jiksnu.triggers.conversation-triggers"
    "jiksnu.sections.conversation-sections"
    "jiksnu.views.conversation-views"]))
