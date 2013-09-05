(ns jiksnu.modules.web.filters.resource-filters
  (:require [ciste.filters :refer [deffilter]]
            [jiksnu.actions.resource-actions :as actions.resource]
            [jiksnu.model.resource :as model.resource]
            [jiksnu.modules.core.filters :refer [parse-page parse-sorting]]))

;; create

(deffilter #'actions.resource/create :http
  [action request]
  (let [{:keys [params]} request]
    (action params)))

(deffilter #'actions.resource/delete :http
  [action request]
  (let [id (:id (:params request))]
    (when-let [item (model.resource/fetch-by-id id)]
      (action item))))

;; index

(deffilter #'actions.resource/index :http
  [action request]
  (action {} (merge {}
                    (parse-page request)
                    (parse-sorting request))))

;; show

(deffilter #'actions.resource/show :http
  [action request]
  (let [{{id :id} :params} request]
    (if-let [item (model.resource/fetch-by-id id)]
     (action item))))

