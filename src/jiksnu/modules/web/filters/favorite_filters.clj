(ns jiksnu.modules.web.filters.favorite-filters
  (:require [ciste.filters :refer [deffilter]]
            [jiksnu.actions.favorite-actions :as actions.favorite]
            [jiksnu.model.user :as model.user]))

(deffilter #'actions.favorite/user-list :http
  [action request]
  (-> request :params :id model.user/fetch-by-id action))
