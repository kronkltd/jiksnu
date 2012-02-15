(ns jiksnu.filters.setting-filters
  (:use (ciste [debug :only [spy]]
               [filters :only [deffilter]])
        (jiksnu [session :only [current-user]])
        jiksnu.actions.setting-actions))

(deffilter #'admin-edit-page :http
  [action request]
  (action))

(deffilter #'avatar-page :http
  [action request]
  (action (current-user)))

(deffilter #'config-output :http
  [action request]
  (action))
