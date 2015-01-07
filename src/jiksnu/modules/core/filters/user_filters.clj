(ns jiksnu.modules.core.filters.user-filters
  (:require [ciste.filters :refer [deffilter]]
            [clojure.tools.logging :as log]
            [jiksnu.actions.user-actions :as actions.user]))

(deffilter #'actions.user/index :page
  [action request]
  (action))

