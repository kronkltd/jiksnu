(ns jiksnu.modules.admin.filters.stream-filters
  (:use [ciste.filters :only [deffilter]]
        [jiksnu.modules.admin.actions.stream-actions :only [index]]
        [jiksnu.modules.core.filters :only [parse-page parse-sorting]])
  (:require [clojure.tools.logging :as log]))

;; index

(deffilter #'index :http
  [action request]
  (action {}
          (merge {}
                 (parse-page request)
                 (parse-sorting request))))
