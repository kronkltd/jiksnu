(ns jiksnu.filters.admin.stream-filters
  (:use [ciste.filters :only [deffilter]]
        [jiksnu.actions.admin.stream-actions :only [index]]
        [jiksnu.filters :only [parse-page parse-sorting]])
  (:require [clojure.tools.logging :as log])
  )

;; index

(deffilter #'index :http
  [action request]
  (action {}
          (merge {}
                 (parse-page request)
                 (parse-sorting request))))
