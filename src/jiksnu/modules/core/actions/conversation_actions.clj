(ns jiksnu.modules.core.actions.conversation-actions
  (:require [ciste.event :as event]
            [clj-time.core :as time]
            [jiksnu.model.conversation :as model.conversation]
            [jiksnu.model.feed-source :as model.feed-source]
            [jiksnu.modules.core.actions.feed-source-actions :as actions.feed-source]
            [jiksnu.modules.core.templates.actions :as templates.actions]
            [jiksnu.transforms :as transforms]
            [jiksnu.transforms.conversation-transforms :as transforms.conversation]
            [slingshot.slingshot :refer [throw+]]
            [taoensso.timbre :as timbre]))

(def model-ns 'jiksnu.model.conversation)

(defonce delete-hooks (ref []))

(defn prepare-create
  [conversation]
  (some-> conversation
          transforms/set-_id
          transforms/set-updated-time
          transforms/set-created-time
          ;; transforms.conversation/set-url
          ;; transforms.conversation/set-update-source
          transforms.conversation/set-domain
          transforms/set-local))

(defn prepare-delete
  ([item]
   (prepare-delete item @delete-hooks))
  ([item hooks]
   (if (seq hooks)
     (recur ((first hooks) item) (rest hooks))
     item)))

(defn create
  [params]
  (if-let [conversation (prepare-create params)]
    (let [conversation (model.conversation/create conversation)]
      (event/notify :conversation-created conversation)
      conversation)
    (throw+ "Could not prepare conversation")))

(defn delete
  [item]
  (let [item (prepare-delete item)]
    (model.conversation/delete item)))

(def index* (templates.actions/make-indexer 'jiksnu.model.conversation))

(defn index
  [& [params & [options]]]
  (index* params options))

(defn fetch-by-group
  [group & [options]]
  (index {:group (:_id group)}))

(defn get-update-source
  [item]
  (when-let [id (:update-source item)]
    (model.feed-source/fetch-by-id id)))

(defn update-record
  [conversation & [options]]
  (if-let [source (get-update-source conversation)]
    (do
      (model.conversation/set-field! conversation :lastUpdated (time/now))
      (actions.feed-source/update-record source options))
    (timbre/error "Could not find update source")))

(defn discover
  [conversation & [options]]
  (timbre/debugf "Discovering conversation: %s" conversation)
  (model.conversation/set-field! conversation :lastDiscovered (time/now))
  conversation)

(defn find-or-create
  [params & [{tries :tries :or {tries 1} :as options}]]
  (if-let [conversation (or (if-let [id (:_id params)]
                              (first (model.conversation/fetch-by-id id)))
                            (if-let [url (:url params)]
                              (first (model.conversation/find-by-url url)))
                            (create params))]
    conversation
    (if (< tries 3)
      (do
        (timbre/info "recurring")
        (find-or-create params (assoc options :tries (inc tries))))
      (throw+ "Could not create conversation"))))

(defn show
  [record]
  record)

(defn add-activity
  [conversation activity]
  (when-not (:parent activity)
    (event/notify ":conversations:parent:set" [conversation activity])
    (model.conversation/set-field! conversation :parent (:_id activity)))
  #_(let [lu (:lastUpdated conversation)
          c (:published activity)]
      (when (or (not lu) (time/before? lu c))
        (timbre/debug "Checking for updated comments")
        (update-record conversation))))

(defn create-new
  []
  (create {:local true}))
