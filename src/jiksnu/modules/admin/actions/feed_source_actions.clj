(ns jiksnu.modules.admin.actions.feed-source-actions
  (:require [jiksnu.actions.feed-source-actions :as actions.feed-source]
            [jiksnu.templates.actions :as templates.actions]))

(def index*
  (templates.actions/make-indexer 'jiksnu.model.feed-source))

(defn index
  [& options]
  (apply index* options))

(defn delete
  [source]
  (actions.feed-source/delete source))

(defn unsubscribe
  [& options]
  (apply actions.feed-source/unsubscribe options))

(defn fetch-updates
  [& args]
  (apply actions.feed-source/update-record args))

(defn add-watcher
  [& options]
  (apply actions.feed-source/add-watcher options))

(defn remove-watcher
  [& options]
  (apply actions.feed-source/remove-watcher options))

(defn show
  [source]
  source)
