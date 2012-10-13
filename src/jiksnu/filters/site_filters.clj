(ns jiksnu.filters.site-filters
  (:use [ciste.filters :only [deffilter]]
        [jiksnu.session :only [current-user-id]]
        jiksnu.actions.site-actions))

(deffilter #'rsd :http
  [action request]
  (action))

(deffilter #'service :http
  [action request]
  (action (current-user-id)))

(deffilter #'get-stats :command
  [action request]
  (action))

(deffilter #'get-stats :http
  [action request]
  (action))
