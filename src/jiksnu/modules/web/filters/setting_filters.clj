(ns jiksnu.modules.web.filters.setting-filters
  (:require [ciste.filters :refer [deffilter]]
            [jiksnu.actions.setting-actions :as actions.setting]
            [jiksnu.session :as session]))

(deffilter #'actions.setting/avatar-page :http
  [action request]
  (action (session/current-user)))

(deffilter #'actions.setting/config-output :http
  [action request]
  (action))

