(ns jiksnu.controller.activity-controller
  (:use ciste.core
        ciste.debug
        ciste.trigger
        clj-tigase.core
        jiksnu.model
        jiksnu.namespace
        jiksnu.session
        [karras.entity :only (make)])
  (:require (jiksnu.model
             [activity :as model.activity]
             [item :as model.item]
             [like :as model.like]
             [subscription :as model.subscription]
             [user :as model.user])
            jiksnu.view)
  (:import jiksnu.model.Activity
           org.apache.abdera.model.Entry))

(defaction index
  [& options]
  (model.activity/index))

(defaction show
  [id]
  (model.activity/show id))

(defaction user-timeline
  [id]
  (let [user (model.user/fetch-by-id id)]
    [user (model.activity/index :authors (make-id id))]))

(defaction delete
  [id]
  (model.activity/delete id))


;; http


(defn index
  [request]
  (model.activity/index))

(defn create
  [{{id "id" :as params} :params :as request}]
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
  (if-let [a (show request)]
    (let [opts
          (merge a
                 (if (= (get (:params request) "public") "public")
                   {:public true}))]
      (model.activity/update opts))))

(defaction delete
  [{{id "id"} :params
    :as request}]
  (model.activity/delete id)
  true)

(defaction edit
  [request]
  (show request))

(defaction new-comment
  [{{id "id"} :params}]
  (model.activity/show id))

(defn user-timeline
  [{{id "id"} :params
    :as request}]
  (let [user (model.user/fetch-by-id id)]
    [user (model.activity/index :authors (make-id id))]))

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

(defn like-activity
  [request]
  (let [{{id "id"} :params} request]
    (if-let [user (current-user)]
      (if-let [activity (model.activity/fetch-by-id id)]
        (model.like/find-or-create activity user)
        true))))

;;;; xmpp

(defn index
  [request]
  (let [to (model.user/get-id (:to request))
        user (model.user/show to)]
    (model.activity/find-by-user user)))

(defn create-activity
  [item]
  (let [entry-string (str (first (children item)))
        entry (jiksnu.view/parse-xml-string entry-string)
        activity (jiksnu.view/to-activity entry)]
    (model.activity/create (make Activity activity))))

;; (defn create
;;   [{:keys [items] :as  request}]
;;   (let [activities (map create-activity items)]
;;     (first activities)))

(defn remote-create
  [request]
  (if (not= (:to request) (:from request))
    (let [packet (:packet request)
          items (children packet "/message/event/items/item")]
      (doseq [entry items]
        (let [activity (jiksnu.view/to-activity
                        (jiksnu.view/parse-xml-string (str entry)))]
          (model.activity/create-raw activity)))
      true)))

(defn fetch-comments
  [{{id "id"} :params :as request}]
  (if-let [activity (model.activity/show id)]
    (map model.activity/show (:comments activity))))

(defn fetch-comments-remote
  [request]
  
  )



