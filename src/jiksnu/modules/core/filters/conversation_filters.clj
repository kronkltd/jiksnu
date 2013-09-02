(ns jiksnu.modules.core.filters.conversation-filters
  (:require [ciste.filters :refer [deffilter]]
            [clojure.tools.logging :as log]
            [jiksnu.actions.conversation-actions :as actions.conversation]))

(deffilter #'actions.conversation/index :page
  [action request]
  (action))
