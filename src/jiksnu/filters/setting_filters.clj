(ns jiksnu.filters.setting-filters
  (:use [ciste.filters :only [deffilter]]
        [jiksnu.session :only [current-user]]
        jiksnu.actions.setting-actions))

(deffilter #'avatar-page :http
  [action request]
  (action (current-user)))

(deffilter #'config-output :http
  [action request]
  (action))

