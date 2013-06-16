(ns jiksnu.actions.conversation-actions
  (:use [ciste.initializer :only [definitializer]]
        [ciste.core :only [defaction]]
        [ciste.loader :only [require-namespaces]]
        [clojure.core.incubator :only [-?> -?>>]]
        [jiksnu.transforms :only [set-_id set-updated-time set-created-time]]
        [slingshot.slingshot :only [throw+]])
  (:require [clj-statsd :as s]
            [clj-time.core :as time]
            [clojure.tools.logging :as log]
            [jiksnu.actions.feed-source-actions :as actions.feed-source]
            [jiksnu.channels :as ch]
            [jiksnu.model :as model]
            [jiksnu.model.conversation :as model.conversation]
            [jiksnu.model.feed-source :as model.feed-source]
            [jiksnu.ops :as ops]
            [jiksnu.templates :as templates]
            [jiksnu.transforms :as transforms]
            [jiksnu.transforms.conversation-transforms :as transforms.conversation]
            [lamina.core :as l]
            [lamina.trace :as trace]))

(defonce delete-hooks (ref []))

(defn prepare-create
  [conversation]
  (-?> conversation
       transforms/set-_id
       transforms/set-updated-time
       transforms/set-created-time
       transforms.conversation/set-url
       transforms.conversation/set-domain
       transforms/set-local
       transforms.conversation/set-update-source))

(defn prepare-delete
  ([item]
     (prepare-delete item @delete-hooks))
  ([item hooks]
     (if (seq hooks)
       (recur ((first hooks) item) (rest hooks))
       item)))

(defaction create
  [params]
  (if-let [conversation (prepare-create params)]
    (model.conversation/create conversation)
    (throw+ "Could not prepare conversation")))

(defaction delete
  [item]
  (let [item (prepare-delete item)]
    (model.conversation/delete item)))

(def index* (templates/make-indexer 'jiksnu.model.conversation))

(defaction index
  [& [params & [options]]]
  (index* params options))

(defn get-update-source
  [item]
  (model.feed-source/fetch-by-id (:update-source item)))

(defaction update
  [conversation & [options]]
  (if-let [source (get-update-source conversation)]
    (do
      (model.conversation/set-field! conversation :lastUpdated (time/now))
      (actions.feed-source/update source options))
    (throw+ "Could not find update source")))

(defaction discover
  [conversation & [options]]
  (log/debugf "Discovering conversation: %s" conversation)
  conversation)

(defaction find-or-create
  [params & [{tries :tries :or {tries 1} :as options}]]
  (if-let [conversation (or (if-let [id (:_id params)]
                              (first (model.conversation/fetch-by-id id)))
                            (if-let [url (:url params)]
                              (first (model.conversation/find-by-url url)))
                            (try
                              (create params)
                              (catch RuntimeException ex
                                (trace/trace "errors:handled" ex))))]
    conversation
    (if (< tries 3)
      (do
        (log/info "recurring")
        (find-or-create params (assoc options :tries (inc tries))))
      (throw+ "Could not create conversation"))))

(defaction show
  [record]
  record)

(defaction add-activity
  [conversation activity]
  (let [lu (:lastUpdated conversation)
        c (:created activity)]
    (when (or (not lu)
              (time/before? lu c))
      (update conversation))))

(defaction create-new
  []
  (create {:local true}))

(definitializer
  (require-namespaces
   ["jiksnu.filters.conversation-filters"
    "jiksnu.triggers.conversation-triggers"
    "jiksnu.sections.conversation-sections"
    "jiksnu.views.conversation-views"]))
