(ns jiksnu.modules.admin.actions.stream-actions
  (:require [jiksnu.templates.actions :as templates.actions]))

(def index*
  (templates.actions/make-indexer 'jiksnu.model.feed-source))

(defn index
  [& options]
  (apply index* options))
