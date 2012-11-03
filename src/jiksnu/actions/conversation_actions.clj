(ns jiksnu.actions.conversation-actions
  (:use [ciste.initializer :only [definitializer]]
        [ciste.core :only [defaction]]
        [ciste.loader :only [require-namespaces]]
        [clojure.core.incubator :only [-?>>]]
        [jiksnu.transforms :only [set-_id set-updated-time set-created-time]]
        [slingshot.slingshot :only [throw+]])
  (:require [clj-statsd :as s]
            [clojure.tools.logging :as log]
            [jiksnu.actions.feed-source-actions :as actions.feed-source]
            [jiksnu.model :as model]
            [jiksnu.model.conversation :as model.conversation]
            [jiksnu.model.feed-source :as model.feed-source]
            [jiksnu.transforms :as transforms]
            [jiksnu.transforms.conversation-transforms :as transforms.conversation]
            [lamina.core :as l]))

(defonce delete-hooks (ref []))

(defn prepare-create
  [conversation]
  (-> conversation
      transforms/set-_id
      transforms.conversation/set-domain
      transforms.conversation/set-local
      transforms.conversation/set-update-source
      transforms/set-updated-time
      transforms/set-created-time))

(defn prepare-delete
  ([item]
     (prepare-delete item @delete-hooks))
  ([item hooks]
     (if (seq hooks)
       (recur ((first hooks) item) (rest hooks))
       item)))

(defaction create
  [params]
  (let [conversation (prepare-create params)]
    (model.conversation/create conversation)))

(defaction delete
  [item]
  (let [item (prepare-delete item)]
    (model.conversation/delete item)))

(def index*
  (model/make-indexer 'jiksnu.model.conversation
                      :sort-clause {:url 1}))

(defaction index
  [& [params & [options]]]
  (index* params options))

(defaction update
  [conversation & [options]]
  (if-let [source (model.feed-source/fetch-by-id (:update-source conversation))]
    (actions.feed-source/update source)
    (throw+ "Could not find update source")))

(defaction discover
  [conversation & [options]]
  (log/debugf "Discovering conversation: %s" conversation)
  conversation)

(defaction find-or-create
  [params]
  (if-let [conversation (or (if-let [id (:_id params)]
                              (first (model.conversation/fetch-by-id id)))
                            (if-let [url (:url params)]
                              (first (:items (model.conversation/find-by-url url)))))]
    conversation
    (create params)))

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
