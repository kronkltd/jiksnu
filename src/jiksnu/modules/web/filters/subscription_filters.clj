(ns jiksnu.modules.core.filters.subscription-filters
  (:require [ciste.filters :refer [deffilter]]
            [clojure.core.incubator :refer [-?> -?>>]]
            [clojure.tools.logging :as log]
            [jiksnu.actions.subscription-actions :as actions.subscription]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.model :as model]
            [jiksnu.model.subscription :as model.subscription]
            [jiksnu.model.user :as model.user]
            [jiksnu.session :as session]
            [jiksnu.util :as util]
            [slingshot.slingshot :refer [throw+]]))

(deffilter #'actions.subscription/delete :http
  [action {{:keys [id]} :params}]
  (when-let [item (model.subscription/fetch-by-id id)]
    (action item)))

;; get-subscribers

(deffilter #'actions.subscription/get-subscribers :http
  [action {{:keys [username id]} :params}]
  (when-let [item (or (when username (model.user/get-user username))
                      (when id (model.user/fetch-by-id id)))]
    (action item)))

;; get-subscriptions

(deffilter #'actions.subscription/get-subscriptions :http
  [action {{:keys [username id]} :params}]
  (when-let [item (or (when username (model.user/get-user username))
                      (when id (model.user/fetch-by-id id)))]
    (action item)))

;; ostatus

(deffilter #'actions.subscription/ostatus :http
  [action request]
  (action))

;; ostatussub

(deffilter #'actions.subscription/ostatussub :http
  [action request]
  (let [{{profile :profile} :params} request]
    (action profile)))

;; ostatussub-submit

(deffilter #'actions.subscription/ostatussub-submit :http
  [action request]
  (if-let [id (-> (log/spy :info request)
                  :params :profile)]
    (action id)
    (log/warn "profile param not found")))

;; show

(deffilter #'show :http
  [action request]
  (let [{{id :id} :params} request]
    (when-let [item (model.subscription/fetch-by-id id)]
      (action item))))

;; subscribe

(deffilter #'actions.subscription/subscribe :http
  [action {{:keys [id subscribeto] :as params} :params}]
  (if-let [id (or id subscribeto)]
    (if-let [target (model.user/fetch-by-id id)]
      (if-let [actor (session/current-user)]
        (action actor target)
        {:view false
         :status 303
         :headers {"Location" (str "/main/ostatussub?subscribeto=" (:_id target))}})
      (throw+ (format "Could not find target with id: %s" id)))
    (throw+ {:type :validation
             :errors {:subscribeto ["Not provided"]}})))

;; unsubscribe

(deffilter #'actions.subscription/unsubscribe :http
  [action request]
  (if-let [actor (session/current-user)]
    (let [params (:params request)]
      (if-let [target (-?> (or (:id params) (:unsubscribeto params))
                           model.user/fetch-by-id)]
        (action actor target)
        (throw+ "User not found")))
    (throw+ "Must be logged in")))

