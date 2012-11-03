(ns jiksnu.filters.setting-filters
  (:use [ciste.filters :only [deffilter]]
        [jiksnu.session :only [current-user]]
        jiksnu.actions.setting-actions)
  (:require [jiksnu.session :as session]))

(deffilter #'avatar-page :http
  [action request]
  (action (session/current-user)))

(deffilter #'config-output :http
  [action request]
  (action))

