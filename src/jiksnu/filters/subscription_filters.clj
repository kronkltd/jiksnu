(ns jiksnu.filters.subscription-filters
  (:use [ciste.filters :only [deffilter]]
        [clojure.core.incubator :only [-?> -?>>]]
        [jiksnu.actions.subscription-actions :only [confirm delete get-subscribers
                                                    get-subscriptions index ostatus ostatussub
                                                    ostatussub-submit remote-subscribe-confirm
                                                    show subscribe subscribed unsubscribe]]
        [jiksnu.session :only [current-user current-user-id]]
        [slingshot.slingshot :only [throw+]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.model :as model]
            [jiksnu.model.subscription :as model.subscription]
            [jiksnu.model.user :as model.user]
            [jiksnu.util :as util])
  (:import tigase.xmpp.JID))

;; delete

(deffilter #'delete :command
  [action id]
  (when-let [item (model.subscription/fetch-by-id (util/make-id id))]
    (action item)))

(deffilter #'delete :http
  [action {{:keys [id]} :params}]
  (when-let [item (model.subscription/fetch-by-id id)]
    (action item)))

;; get-subscribers

(deffilter #'get-subscribers :http
  [action {{:keys [username id]} :params}]
  (when-let [item (or (when username (model.user/get-user username))
                      (when id (model.user/fetch-by-id (util/make-id id))))]
    (action item)))

(deffilter #'get-subscribers :page
  [action request]
  (let [item (:item request)]
    (action item)))

;; get-subscriptions

(deffilter #'get-subscriptions :http
  [action {{:keys [username id]} :params}]
  (when-let [item (or (when username (model.user/get-user username))
                      (when id (model.user/fetch-by-id (util/make-id id))))]
    (action item)))

(deffilter #'get-subscriptions :page
  [action request]
  (let [item (:item request)]
    (action item)))

;; index

(deffilter #'index :page
  [action request]
  (action))

;; ostatus

(deffilter #'ostatus :http
  [action request]
  (action))

;; ostatussub

(deffilter #'ostatussub :http
  [action request]
  (let [{{profile :profile} :params} request]
    (action profile)))

;; ostatussub-submit

(deffilter #'ostatussub-submit :http
  [action request]
  (-> request :params :profile action))

;; show

(deffilter #'show :http
  [action request]
  (let [{{id :id} :params} request]
    (when-let [item (model.subscription/fetch-by-id (util/make-id id))]
      (action item))))

;; subscribe

(deffilter #'subscribe :http
  [action {{:keys [id subscribeto] :as params} :params}]
  (if-let [id (or id subscribeto)]
    (if-let [target (model.user/fetch-by-id (util/make-id id))]
      (if-let [actor (current-user)]
        (action actor target)
        {:view false
         :status 303
         :headers {"Location" (str "/main/ostatussub?subscribeto=" (:_id target))}})
      (throw+ (format "Could not find target with id: %s" id)))
    (throw+ {:type :validation
             :errors {:subscribeto ["Not provided"]}})))

;; unsubscribe

(deffilter #'unsubscribe :http
  [action request]
  (if-let [actor (current-user)]
    (let [params (:params request)]
      (if-let [target (-?> (or (:id params) (:unsubscribeto params))
                           util/make-id
                           model.user/fetch-by-id)]
        (action actor target)
        (throw+ "User not found")))
    (throw+ "Must be logged in")))

