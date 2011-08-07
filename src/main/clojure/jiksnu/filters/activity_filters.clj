(ns jiksnu.filters.activity-filters
  (:use aleph.http
        ciste.debug
        ciste.filters
        clj-tigase.core
        jiksnu.abdera
        jiksnu.actions.activity-actions
        jiksnu.helpers.activity-helpers
        jiksnu.model
        jiksnu.sections.activity-sections
        jiksnu.session
        lamina.core)
  (:require [clj-tigase.element :as element]
            [clojure.java.io :as io]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.like :as model.like]
            [jiksnu.model.user :as model.user]))

(deffilter #'add-comment :http
  [action request]
  (action (:params request)))

(deffilter #'comment-response :xmpp
  [action request]
  (if (not= (:to request) (:from request))
    (let [packet (:packet request)
          items (:items request)]
      (action (map #(to-activity
                     (parse-xml-string
                      (str (first (element/children %)))))
                   items)))))

(deffilter #'delete :http
  [action request]
  (let [{{id :id} :params} request
        activity (model.activity/show id)]
    (action activity)))

(deffilter #'edit :http
  [action request]
  (let [{{id :id} :params} request]
    (action id)))

(deffilter #'fetch-comments :http
  [action request]
  (let [{{id :id} :params} request]
    (if-let [activity (model.activity/show id)]
      (action activity))))

(deffilter #'fetch-comments :xmpp
  [action request]
  (let [{{id :id} :params} request]
    (if-let [activity (model.activity/show id)]
      (action activity))))

(deffilter #'fetch-comments-remote :xmpp
  [action request])

(deffilter #'friends-timeline :http
  [action request]
  (let [{{id :id} :params} request]
    (model.activity/index :author id)))

(deffilter #'inbox :http
  [action request]
  [])

(deffilter #'index :http
  [action request]
  (action))

(deffilter #'index :xmpp
  [action request]
  (action))

(deffilter #'like-activity :http
  [action request]
  (let [{{id :id} :params} request]
    (if-let [user (current-user)]
      (if-let [activity (model.activity/fetch-by-id id)]
        (model.like/find-or-create activity user)
        true))))

(deffilter #'new :http
  [action request]
  (action))

(deffilter #'new-comment :http
  [action request]
  (let [{{id :id} :params} request]
    (model.activity/show id)))

(deffilter #'post :http
  [action request]
  (let [{params :params} request
        p (-> params (dissoc :*))]
    (action p)))

(deffilter #'post :xmpp
  [action request]
  (let [{:keys [items]} request
        activities
        (map
         (fn [item]
           (-> item element/children first
               str parse-xml-string
               to-activity))
         items)]
    (action (first activities))))

(deffilter #'show :http
  [action request]
  (let [{{id :id} :params} request]
    (action (make-id id))))

(deffilter #'show :xmpp
  [action request]
  (let [{:keys [items]} request
        ids (map #(.getAttribute % "id") items)
        id (first ids)]
    (action (make-id id))))

(deffilter #'remote-create :xmpp
  [action request]
  (if (not= (:to request) (:from request))
    (let [packet (:packet request)
          ;; items (element/children packet "/message/event/items/item")
          items (map (comp first element/children) (:items request))]
      (action (map #(to-activity (parse-xml-string (str %)))
            items)))))

(deffilter #'update :http
  [action request]
  (let [{params :params} request]
    (action params)))

(deffilter #'user-timeline :http
  [action request]
  (let [{{id :id} :params} request]
    (let [user (model.user/fetch-by-id id)]
      (action user))))

(deffilter #'user-timeline :xmpp
  [action request]
  (let [user (model.user/fetch-by-jid (:to request))]
    (action user)))
