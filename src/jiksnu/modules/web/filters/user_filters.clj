(ns jiksnu.modules.web.filters.user-filters
  (:require [ciste.filters :refer [deffilter]]
            [taoensso.timbre :as log]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.model.user :as model.user]
            [jiksnu.modules.core.filters :refer [parse-page parse-sorting]]
            [jiksnu.session :as session]
            [slingshot.slingshot :refer [throw+]]))

(deffilter #'actions.user/add-stream :http
  [action request]
  (let [params (:params request)
        id (:id params)
        params (dissoc params :id)
        user (model.user/fetch-by-id id)]
    (action user params)))

(deffilter #'actions.user/profile :http
  [action request]
  (if-let [user (session/current-user)]
    user (log/error "no user")))

(deffilter #'actions.user/register :http
  [action {{:keys [username password confirm-password] :as params} :params}]
  (if (:accepted params)
    (if (= password confirm-password)
      (action params)
      (throw+ "Password and confirm password do not match"))
    (throw+ "you didn't check the box")))

(deffilter #'actions.user/show :http
  [action request]
  (when-let [params (:params request)]
    (let [id (or (let [{:keys [user domain]} params]
                   (when (and user domain)
                     (str user "@" domain)))
                 (:* params)
                 (:id params))
          username (:username params)]
      (when (or id username)
        (when-let [user (or (and id       (model.user/fetch-by-id id))
                            (and username (model.user/get-user username)))]
          (action user))))))

(deffilter #'actions.user/show-basic :http
  [action request]
  (when-let [params (:params request)]
    (let [id (:id params)
          username (:username params)]
      (when (or id username)
        (when-let [user (or (and id       (model.user/fetch-by-id id))
                            (and username (model.user/get-user username)))]
          (action user))))))

(deffilter #'actions.user/update-record :http
  [action request]
  (let [{{id :id} :params} request]
    (if-let [user (model.user/fetch-by-id id)]
     (action user {:force true}))))

(deffilter #'actions.user/update-profile :http
  [action {params :params}]
  (action params))
