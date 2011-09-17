(ns jiksnu.filters.activity-filters
  (:use (ciste [debug :only (spy)]
               [filters :only (deffilter)])
        (jiksnu model session)
        jiksnu.actions.activity-actions
        lamina.core)
  (:require (aleph [http :as http])
            (clj-tigase [core :as tigase]
                        [element :as element])
            (clojure.java [io :as io])
            (jiksnu [abdera :as abdera])
            (jiksnu.helpers [activity-helpers :as helpers.activity])
            (jiksnu.sections [activity-sections :as sections.activity])
            (jiksnu.model [activity :as model.activity]
                          [like :as model.like]
                          [user :as model.user])))

(deffilter #'add-comment :http
  [action request]
  (action (:params request)))

(deffilter #'delete :http
  [action request]
  (-> request :params :id
      model.activity/show action))

(deffilter #'edit :http
  [action request]
  (let [{{id :id} :params} request]
    (action id)))

(deffilter #'fetch-comments :http
  [action request]
  (let [{{id :id} :params} request]
    (if-let [activity (model.activity/show id)]
      (action activity))))

(deffilter #'friends-timeline :http
  [action request]
  (->> request :params :id
       (model.activity/index :author)))

(deffilter #'inbox :http
  [action request]
  [])

(deffilter #'index :http
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
  (-> request :params :id model.activity/show))

(deffilter #'post :http
  [action request]
  (-> request :params (dissoc :*) action))

(deffilter #'show :http
  [action request]
  (-> request :params :id
      make-id action))

(deffilter #'update :http
  [action request]
  (-> request :params action))

(deffilter #'user-timeline :http
  [action request]
  (-> request :params :username
      model.user/show action))








(deffilter #'comment-response :xmpp
  [action request]
  (if (not= (:to request) (:from request))
    (let [packet (:packet request)
          items (:items request)]
      (action (map #(entry->activity
                     (abdera/parse-xml-string
                      (str (first (element/children %)))))
                   items)))))

(deffilter #'fetch-comments :xmpp
  [action request]
  (let [{{id :id} :params} request]
    (if-let [activity (model.activity/show id)]
      (action activity))))

(deffilter #'fetch-comments-remote :xmpp
  [action request])

(deffilter #'index :xmpp
  [action request]
  (action))

(deffilter #'post :xmpp
  [action request]
  (let [{:keys [items]} request
        activities
        (map
         (fn [item]
           (-> item element/children first
               str abdera/parse-xml-string
               entry->activity))
         items)]
    (action (first activities))))

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
      (action (map #(entry->activity
                     (abdera/parse-xml-string (str %)))
            items)))))

(deffilter #'user-timeline :xmpp
  [action request]
  (-> request :to
      model.user/fetch-by-jid action))
