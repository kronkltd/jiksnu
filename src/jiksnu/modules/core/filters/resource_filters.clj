(ns jiksnu.modules.core.filters.resource-filters
  (:require [ciste.filters :refer [deffilter]]
            [clojure.tools.logging :as log]
            [jiksnu.actions.resource-actions :as actions.resource]))

(deffilter #'actions.resource/index :page
  [action request]
  (action))

