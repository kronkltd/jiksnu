(ns jiksnu.modules.admin.actions.stream-actions
  (:use [ciste.core :only [defaction]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.actions.feed-source-actions :as actions.feed-source]
            [jiksnu.model :as model]
            [jiksnu.model.feed-source :as model.feed-source]
            [jiksnu.templates.actions :as templates.actions])
  )

(def index*
  (templates.actions/make-indexer 'jiksnu.model.feed-source))

(defaction index
  [& options]
  (apply index* options))

