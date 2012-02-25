(ns jiksnu.filters.feed-source-filters
  (:use (ciste [debug :only [spy]]
               [filters :only [deffilter]])
        jiksnu.actions.feed-source-actions)
  (:require (jiksnu.model [user :as model.user])))

(deffilter #'process-updates :http
  [action request]
  (action request))

