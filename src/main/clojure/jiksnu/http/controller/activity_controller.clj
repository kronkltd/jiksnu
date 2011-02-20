(ns jiksnu.http.controller.activity-controller
  (:use jiksnu.model
        jiksnu.session
        [karras.entity :only (make)])
  (:require [jiksnu.model.activity :as model.activity]
            [jiksnu.model.item :as model.item]
            [jiksnu.model.subscription :as model.subscription]
            [jiksnu.model.user :as model.user])
  (:import jiksnu.model.Activity))

(defn index
  [request]
  (model.activity/index))

(defn create
  [{{id "id" :as params} :params}]
  (let [a (make Activity params)]
    (let [created-activity (model.activity/create a)
          user (current-user)
          subscribers (model.subscription/subscribers user)]
      (doseq [subscription subscribers]
        (let [u (model.user/fetch-by-id (:from subscription))]
          (println "u: " u)
          (model.item/push u created-activity)))
      (model.item/push user created-activity)
      created-activity)))

(defn new
  [request]
  (Activity.))

(defn show
  [{{id "id"} :params
    :as request}]
  (model.activity/show id))

(defn update
  [request]
  (let [a (show request)]
    a))

(defn delete
  [{{id "id"} :params
    :as request}]
  (model.activity/delete id))

(defn edit
  [request]
  (show request))

(defn user-timeline
  [{{id "id"} :params
    :as request}]
  (model.activity/index :authors id))

(defn friends-timeline
  [{{id "id"} :params
    :as request}]
  (model.activity/index :authors id))

(defn inbox
  [request]
  [])
