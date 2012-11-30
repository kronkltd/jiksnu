(ns jiksnu.actions.admin.feed-source-actions
  (:use [ciste.core :only [defaction]]
        [ciste.initializer :only [definitializer]]
        [ciste.loader :only [require-namespaces]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.actions.feed-source-actions :as actions.feed-source]
            [jiksnu.model :as model]
            [jiksnu.model.feed-source :as model.feed-source]))

(def index*
  (model/make-indexer 'jiksnu.model.feed-source))

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
  (apply actions.feed-source/update args))

(defaction add-watcher
  [& options]
  (apply actions.feed-source/add-watcher options))

(defaction remove-watcher
  [& options]
  (apply actions.feed-source/remove-watcher options))

(defaction show
  [source]
  source)

(definitializer
  (require-namespaces
   ["jiksnu.filters.admin.feed-source-filters"
    "jiksnu.sections.feed-source-sections"
    "jiksnu.views.admin.feed-source-views"]))
