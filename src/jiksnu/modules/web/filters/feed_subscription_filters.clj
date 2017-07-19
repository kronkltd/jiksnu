(ns jiksnu.modules.web.filters.feed-subscription-filters
  (:require [ciste.filters :refer [deffilter]]
            [jiksnu.modules.core.actions.feed-subscription-actions :as actions.feed-subscription]
            [jiksnu.modules.core.model.feed-subscription :as model.feed-subscription]))

(deffilter #'actions.feed-subscription/show :http
  [action request]
  (let [{{id :id} :params} request]
    (if-let [user (model.feed-subscription/fetch-by-id id)]
      (action user))))
