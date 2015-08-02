(ns jiksnu.modules.admin.actions.feed-source-actions
  (:require [ciste.core :refer [defaction]]
            [clojure.tools.logging :as log]
            [jiksnu.actions.feed-source-actions :as actions.feed-source]
            [jiksnu.model :as model]
            [jiksnu.model.feed-source :as model.feed-source]
            [jiksnu.templates.actions :as templates.actions]))

(def index*
  (templates.actions/make-indexer 'jiksnu.model.feed-source))

(defaction index
  [& options]
  (apply index* options))

(defn delete
  [source]
  (actions.feed-source/delete source))

(defaction unsubscribe
  [& options]
  (apply actions.feed-source/unsubscribe options))

(defaction fetch-updates
  [& args]
  (apply actions.feed-source/update-record args))

(defaction add-watcher
  [& options]
  (apply actions.feed-source/add-watcher options))

(defaction remove-watcher
  [& options]
  (apply actions.feed-source/remove-watcher options))

(defaction show
  [source]
  source)
