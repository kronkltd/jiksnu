(ns jiksnu.actions.conversation-actions
  (:require [ciste.core :refer [defaction]]
            [clj-time.core :as time]
            [clojure.tools.logging :as log]
            [jiksnu.actions.feed-source-actions :as actions.feed-source]
            [jiksnu.channels :as ch]
            [jiksnu.model :as model]
            [jiksnu.model.conversation :as model.conversation]
            [jiksnu.model.feed-source :as model.feed-source]
            [jiksnu.ops :as ops]
            [jiksnu.templates.actions :as templates.actions]
            [jiksnu.transforms :as transforms]
            [jiksnu.transforms.conversation-transforms :as transforms.conversation]
            [lamina.trace :as trace]
            [slingshot.slingshot :refer [throw+]]))

(defonce delete-hooks (ref []))

(defn prepare-create
  [conversation]
  (some-> conversation
          transforms/set-_id
          transforms/set-updated-time
          transforms/set-created-time
          ;; transforms.conversation/set-url
          transforms.conversation/set-domain
          transforms/set-local
          ;; transforms.conversation/set-update-source
          ))

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

(def index* (templates.actions/make-indexer 'jiksnu.model.conversation))

(defaction index
  [& [params & [options]]]
  (index* params options))

(defaction fetch-by-group
  [group & [options]]
  (index {:group (:_id group)}))

(defn get-update-source
  [item]
  (when-let [id (:update-source item)]
    (model.feed-source/fetch-by-id id)))

(defaction update-record
  [conversation & [options]]
  (if-let [source (get-update-source conversation)]
    (do
      (model.conversation/set-field! conversation :lastUpdated (time/now))
      (actions.feed-source/update-record source options))
    (log/error "Could not find update source")))

(defaction discover
  [conversation & [options]]
  (log/debugf "Discovering conversation: %s" conversation)
  (model.conversation/set-field! conversation :lastDiscovered (time/now))
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
  (when-not (:parent activity)
    (trace/trace :conversations:parent:set [conversation activity])
    (model.conversation/set-field! conversation :parent (:_id activity)))
  #_(let [lu (:lastUpdated conversation)
          c (:published activity)]
      (when (or (not lu) (time/before? lu c))
        (log/debug "Checking for updated comments")
        (update-record conversation))))

(defaction create-new
  []
  (create {:local true}))

