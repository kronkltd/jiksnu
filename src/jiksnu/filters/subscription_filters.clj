(ns jiksnu.filters.subscription-filters
  (:use [ciste.filters :only [deffilter]]
        [clojure.core.incubator :only [-?> -?>>]]
        [jiksnu.actions.subscription-actions :only [delete
                                                    get-subscribers get-subscriptions
                                                    ostatus ostatussub ostatussub-submit
                                                    remote-subscribe-confirm
                                                    subscribe subscribed
                                                    confirm unsubscribe]]
        [jiksnu.session :only [current-user current-user-id]]
        [slingshot.slingshot :only [throw+]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.model :as model]
            [jiksnu.model.subscription :as model.subscription]
            [jiksnu.model.user :as model.user]))

;; delete

(deffilter #'delete :command
  [action id]
  (let [item (model.subscription/fetch-by-id (model/make-id id))]
    (action item)))

(deffilter #'delete :http
  [action request]
  (-?> request :params :id model.subscription/fetch-by-id action))

;; get-subscribers

(deffilter #'get-subscribers :http
  [action request]
  (let [{{:keys [username id]} :params} request]
    (if-let [user (or (when username (model.user/get-user username))
                      (when id (model.user/fetch-by-id (model/make-id id))))]
      (action user))))

(deffilter #'get-subscribers :xmpp
  [action request]
  (if-let [user (model.user/fetch-by-jid (:to request))]
    (action user)))

;; get-subscriptions

(deffilter #'get-subscriptions :http
  [action request]
  (let [{{:keys [username id]} :params} request]
    (if-let [user (or (when username (model.user/get-user username))
                      (when id (model.user/fetch-by-id (model/make-id id))))]
     (action user))))

(deffilter #'get-subscriptions :xmpp
  [action request]
  (if-let [user (model.user/fetch-by-jid (:to request))]
    (action user)))

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

;; remote-subscribe-confirm

(deffilter #'remote-subscribe-confirm :xmpp
  [action request]
  (let [subscriber (model.user/fetch-by-jid (:to request))
        subscribee (model.user/fetch-by-jid (:from request))]
    (if-let [subscription (model.subscription/fetch-all
                           {:to (:_id subscribee) :from (:_id subscriber)})]
      ;; TODO: this should call the action
      (confirm subscription))))

;; subscribe

(deffilter #'subscribe :http
  [action request]
  (let [params (:params request)]
    (if-let [id (or (:id params) (:subscribeto params))]
      (if-let [target (-> id model/make-id model.user/fetch-by-id)]
        (if-let [actor (current-user)]
          (action actor target)
          {:view false
           :status 303
           :headers {"Location" (str "/main/ostatussub?subscribeto=" (:_id target))}})
        (throw+ (format "Could not find target with id: %s" id)))
      (throw+ {:type :validation
               :errors {:subscribeto ["Not provided"]}}))))

(deffilter #'subscribe :xmpp
  [action request]
  (if-let [user (model.user/fetch-by-jid (:to request))]
    (action user)))

;; subscribed

(deffilter #'subscribed :xmpp
  [action request]
  (if-let [subscriber (model.user/fetch-by-jid (:from request))]
    (if-let [subscribee (model.user/fetch-by-jid (:to request))]
      (action subscriber subscribee))))

;; unsubscribe

(deffilter #'unsubscribe :http
  [action request]
  (if-let [actor (current-user)]
    (let [params (:params request)]
      (if-let [target (-?> (or (:id params) (:unsubscribeto params))
                           model/make-id
                           model.user/fetch-by-id)]
        (action actor target)
        (throw+ "User not found")))
    (throw+ "Must be logged in")))

(deffilter #'unsubscribe :xmpp
  [action request]
  (let [{:keys [to from]} request]
    (if-let [user (model.user/fetch-by-jid to)]
      (if-let [subscriber (actions.user/find-or-create-by-jid from)]
        (action (:_id subscriber) (:_id user))))))
