(ns jiksnu.modules.admin.filters.activity-filters
  (:require [ciste.filters :refer [deffilter]]
            [clojure.tools.logging :as log]
            [jiksnu.modules.admin.actions.activity-actions :refer [index]]
            [jiksnu.modules.core.filters :refer [parse-page parse-sorting]]))

(deffilter #'index :http
  [action request]
  (action {}
          (merge {}
                 (parse-page request)
                 (parse-sorting request))))
