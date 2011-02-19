(ns jiksnu.http.controller.activity-controller
  (:use jiksnu.model
        [karras.entity :only (make)])
  (:require [jiksnu.model.activity :as model.activity])
  (:import jiksnu.model.Activity))

(defn index
  [request]
  (model.activity/index))

(defn create
  [{{id "id" :as params} :params}]
  (let [a (make Activity params)]
    (model.activity/create a)))

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
