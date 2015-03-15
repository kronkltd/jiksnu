(ns jiksnu.modules.admin.filters.like-filters
  (:require [ciste.filters :refer [deffilter]]
            [jiksnu.model :as model]
            [jiksnu.model.like :as model.like]
            [jiksnu.modules.admin.actions.like-actions :refer [delete index]]
            [jiksnu.util :as util]))

(deffilter #'index :http
  [action request]
  ;; TODO: pass page params
  (action))

(deffilter #'delete :http
  [action request]
  (action (-> request :params :id model.like/fetch-by-id)))
