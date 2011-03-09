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
  [{{id "id" :as params} :params :as request}]
  (let [a (make Activity params)]
    (let [created-activity (model.activity/create a)]
      #_(if (:parent created-activity)
          (jiksnu.http.view.activity-view/notify-commented
           request created-activity))
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
  (if-let [a (show request)]
    (let [opts
          (merge a
                 (if (= (get (:params request) "public") "public")
                   {:public true}))]
      (model.activity/update opts))))

(defn delete
  [{{id "id"} :params
    :as request}]
  (model.activity/delete id)
  true)

(defn edit
  [request]
  (show request))

(defn new-comment
  [{{id "id"} :params}]
  (model.activity/show id))

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

(defn fetch-comments
  [{{id "id"} :params :as request}]
  (if-let [activity (model.activity/show id)]
    (if-let [author (model.user/fetch-by-id (first (:authors activity)))]
      activity)))
