(ns jiksnu.modules.core.filters.user-filters
  (:require [ciste.config :refer [config]]
            [ciste.filters :refer [deffilter]]
            [clojure.tools.logging :as log]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.model.user :as model.user]
            [jiksnu.modules.core.filters :refer [parse-page parse-sorting]]
            [jiksnu.session :as session]
            [jiksnu.util :as util]
            [slingshot.slingshot :refer [throw+]]))

(deffilter #'actions.user/add-stream :http
  [action request]
  (let [params (:params request)
        id (:id params)
        params (dissoc params :id)
        user (model.user/fetch-by-id id)]
    (action user params)))

;; create

(deffilter #'actions.user/create :http
  [action request]
  (let [{:keys [params]} request]
    (action params)))

;; delete

(deffilter #'actions.user/delete :http
  [action request]
  (let [id (:id (:params request))]
    (when-let [item (model.user/fetch-by-id id)]
      (action item))))

;; discover

(deffilter #'actions.user/discover :http
  [action request]
  (let [{{id :id} :params} request]
    (if-let [user (model.user/fetch-by-id id)]
      (action user {:force true}))))

;; index

(deffilter #'actions.user/index :http
  [action request]
  (action {} (merge {}
                    (parse-page request)
                    (parse-sorting request))))

(deffilter #'actions.user/index :page
  [action request]
  (action))

;; profile

(deffilter #'actions.user/profile :http
  [action request]
  (if-let [user (session/current-user)]
    user (log/error "no user")))

;; register

(deffilter #'actions.user/register :http
  [action {{:keys [username password confirm-password] :as params} :params}]
  (if (:accepted params)
    (if (= password confirm-password)
      (action params)
      (throw+ "Password and confirm password do not match"))
    (throw+ "you didn't check the box")))

;; register-page

(deffilter #'actions.user/register-page :http
  [action request]
  (action))

;; show

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



;; update

(deffilter #'actions.user/update :http
  [action request]
  (let [{{id :id} :params} request]
    (if-let [user (model.user/fetch-by-id id)]
     (action user {:force true}))))

;; update-profile

(deffilter #'actions.user/update-profile :http
  [action {params :params}]
  (action params))

