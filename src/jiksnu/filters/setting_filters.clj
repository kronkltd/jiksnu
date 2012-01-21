(ns jiksnu.filters.setting-filters
  (:use (ciste [debug :only (spy)]
               filters)
        (jiksnu [session :only [current-user]])
        jiksnu.actions.setting-actions))

(deffilter #'admin-edit-page :http
  [action request]
  (action))

(deffilter #'avatar-page :http
  [action request]
  (action (current-user)))
