(ns jiksnu.modules.admin.actions.stream-actions
  (:require [jiksnu.actions.feed-source-actions :as actions.feed-source]
            [jiksnu.model.feed-source :as model.feed-source]
            [jiksnu.templates.actions :as templates.actions]))

(def index*
  (templates.actions/make-indexer 'jiksnu.model.feed-source))

(defn index
  [& options]
  (apply index* options))
