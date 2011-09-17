(ns jiksnu.filters.activity-filters
  (:use (ciste [config :only (config)]
               [debug :only (spy)]
               [filters :only (deffilter)])
        (jiksnu model session)
        jiksnu.actions.activity-actions
        lamina.core)
  (:require (aleph [http :as http])
            (clj-tigase [core :as tigase]
                        [element :as element])
            (clojure.java [io :as io])
            (jiksnu [abdera :as abdera])
            (jiksnu.actions [user-actions :as actions.user])
            (jiksnu.helpers [activity-helpers :as helpers.activity])
            (jiksnu.sections [activity-sections :as sections.activity])
            (jiksnu.model [activity :as model.activity]
                          [like :as model.like]
                          [user :as model.user])))

(deffilter #'add-comment :http
  [action request]
  (-> request :params action))

(deffilter #'delete :http
  [action request]
  (-> request :params :id show action))

(deffilter #'edit :http
  [action request]
  (-> request :params :id show action))

(deffilter #'fetch-comments :http
  [action request]
  (-> request :params :id show action))

(deffilter #'friends-timeline :http
  [action request]
  (-> request :params :id model.user/show action))

(deffilter #'inbox :http
  [action request]
  (action))

(deffilter #'index :http
  [action request]
  (action))

(deffilter #'like-activity :http
  [action request]
  (-> request :params :id show action))

(deffilter #'new :http
  [action request]
  (action))

(deffilter #'new-comment :http
  [action request]
  (-> request :params :id model.activity/show))

(deffilter #'post :http
  [action request]
  (-> request :params action))

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
      model.user/show
      action))








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
      actions.user/fetch-by-jid action))
