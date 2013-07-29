(ns jiksnu.filters.user-filters
  (:use [ciste.config :only [config]]
        [ciste.filters :only [deffilter]]
        jiksnu.actions.user-actions
        [jiksnu.filters :only [parse-page parse-sorting]]
        [slingshot.slingshot :only [throw+]])
  (:require [ciste.model :as cm]
            [clj-tigase.element :as element]
            [clojure.tools.logging :as log]
            [jiksnu.model :as model]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.subscription :as model.subscription]
            [jiksnu.model.user :as model.user]
            [jiksnu.session :as session]
            [jiksnu.util :as util]))

;; create

(deffilter #'create :http
  [action request]
  (let [{:keys [params]} request]
    (action params)))

;; delete

(deffilter #'delete :command
  [action id]
  (when-let [item (model.user/fetch-by-id (util/make-id id))]
    (action item)))

(deffilter #'delete :http
  [action request]
  (let [id (:id (:params request))]
    (when-let [item (model.user/fetch-by-id (util/make-id id))]
      (action item))))

;; discover

(deffilter #'discover :command
  [action id]
  (if-let [item (model.user/fetch-by-id (util/make-id id))]
    (action item {:force true})))

(deffilter #'discover :http
  [action request]
  (let [{{id :id} :params} request]
    (if-let [user (model.user/fetch-by-id (util/make-id id))]
      (action user {:force true}))))

;; index

(deffilter #'index :http
  [action request]
  (action {} (merge {}
                    (parse-page request)
                    (parse-sorting request))))

(deffilter #'index :page
  [action request]
  (action))

;; profile

(deffilter #'profile :http
  [action request]
  (if-let [user (session/current-user)]
    user (log/error "no user")))

;; register

(deffilter #'register :http
  [action {{:keys [username password confirm-password] :as params} :params}]
  (if (:accepted params)
    (if (= password confirm-password)
      (action params)
      (throw+ "Password and confirm password do not match"))
    (throw+ "you didn't check the box")))

;; register-page

(deffilter #'register-page :http
  [action request]
  (action))

;; show

(deffilter #'show :http
  [action request]
  (when-let [params (:params request)]
    (let [id (:id params)
          username (:username params)]
      (when (or id username)
        (when-let [user (or (and id       (model.user/fetch-by-id id))
                            (and username (model.user/get-user username)))]
          (action user))))))

;; subscribe

(deffilter #'subscribe :command
  [action id]
  (when-let [item (model.user/fetch-by-id id)]
    (action item)))

;; update

(deffilter #'update :http
  [action request]
  (let [{{id :id} :params} request]
    (if-let [user (model.user/fetch-by-id (util/make-id id))]
     (action user {:force true}))))

(deffilter #'update :command
  [action id]
  (let [item (model.user/fetch-by-id (util/make-id id))]
    (action item {:force true})))

;; update-profile

(deffilter #'update-profile :http
  [action {params :params}]
  (action params))

;; user-meta

(deffilter #'user-meta :http
  [action request]
  (->> request :params :uri
       util/split-uri
       (apply model.user/get-user)
       action))

;; (deffilter #'update-hub :http
;;   [action request]
;;   (let [{params :params} request
;;         {username :id} params
;;         user (model.user/fetch-by-id username)]
;;     (action user)))

