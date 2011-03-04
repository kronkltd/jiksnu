(ns jiksnu.http.controller.activity-controller
  (:use jiksnu.model
        jiksnu.session
        [karras.entity :only (make)])
  (:require [jiksnu.model.activity :as model.activity]
            [jiksnu.model.item :as model.item]
            [jiksnu.model.subscription :as model.subscription]
            [jiksnu.model.user :as model.user]
            jiksnu.xmpp.view.activity-view)
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
          (model.item/push u created-activity)
          (jiksnu.xmpp.view.activity-view/notify u created-activity)
          ))
      (model.item/push user created-activity)
      (jiksnu.xmpp.view.activity-view/notify user created-activity)
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
    (do (println "a: " a)
        (model.activity/update
         (merge a
                (if (= (get (:params request) "public") "public")
                  {:public true}))))))

(defn delete
  [{{id "id"} :params
    :as request}]
  (model.activity/delete id)
  true)

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
