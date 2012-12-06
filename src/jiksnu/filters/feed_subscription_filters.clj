(ns jiksnu.filters.feed-subscription-filters
  (:use [ciste.filters :only [deffilter]]
        [jiksnu.actions.feed-subscription-actions :only [show]]
        [jiksnu.filters :only [parse-page parse-sorting]])
  (:require [jiksnu.model :as model]
            [jiksnu.model.feed-subscription :as model.feed-subscription]))

;; show

(deffilter #'show :http
  [action request]
  (let [{{id :id} :params} request]
    (if-let [user (model.feed-subscription/fetch-by-id (model/make-id id))]
     (action user))))

