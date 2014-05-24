(ns jiksnu.modules.web.filters.group-filters
  (:require [ciste.filters :refer [deffilter]]
            [clojure.tools.logging :as log]
            [jiksnu.actions.group-actions :as actions.group]
            [jiksnu.model.group :as model.group]
            [jiksnu.model.user :as model.user]))

(deffilter #'actions.group/add :http
  [action request]
  (let [params (:params request)]
    (action params)))

(deffilter #'actions.group/create :http
  [action request]
  (let [params (:params request)]
    (action params)))

(deffilter #'actions.group/edit-page :http
  [action request]
  (let [{{:keys [id name]} :params} request]
    (when-let [item (or (model.group/fetch-by-id id)
                        (model.group/fetch-by-name name))]
      (action item))))

(deffilter #'actions.group/fetch-by-user :http
  [action request]
  (when-let [params (:params request)]
    (let [id (:id params)
          username (:username params)]
      (when (or id username)
        (when-let [user (or (and id       (model.user/fetch-by-id id))
                            (and username (model.user/get-user username)))]
          (action user))))))

(deffilter #'actions.group/index :http
  [action request]
  (action))

(deffilter #'actions.group/join :http
  [action request]
  (let [{{:keys [id name]} :params} request]
    (when-let [item (or (when id (model.group/fetch-by-id id))
                        (when name (model.group/fetch-by-name name)))]
      (action item))))

(deffilter #'actions.group/new-page :http
  [action request]
  (action))

(deffilter #'actions.group/show :http
  [action request]
  (let [{{:keys [id name]} :params} request]
    (when-let [item (or (when id (model.group/fetch-by-id id))
                        (when name (model.group/fetch-by-name name)))]
      (action item))))

(deffilter #'actions.group/user-list :http
  [action request]
  (-> request :params :id
      model.user/fetch-by-id action))

