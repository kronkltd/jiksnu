(ns jiksnu.modules.web.filters.conversation-filters
  (:require [ciste.filters :refer [deffilter]]
            [taoensso.timbre :as log]
            [jiksnu.actions.conversation-actions :as actions.conversation]
            [jiksnu.model.conversation :as model.conversation]
            [jiksnu.modules.core.filters :refer [parse-page parse-sorting]]))

(deffilter #'actions.conversation/create :http
  [action request]
  (-> request :params action))

(deffilter #'actions.conversation/delete :http
  [action request]
  (-> request :params :id model.conversation/fetch-by-id action))

(deffilter #'actions.conversation/index :http
  [action request]
  (let [options (merge {}
                       (parse-page request)
                       (parse-sorting request))]
    (action {} options)))

(deffilter #'actions.conversation/show :http
  [action request]
  (when-let [id (:id (:params request))]
    (when-let [item (model.conversation/fetch-by-id id)]
      (action item))))
