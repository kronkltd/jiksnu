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
            (jiksnu.sections [activity-sections :as sections.activity])
            (jiksnu.model [activity :as model.activity]
                          [like :as model.like]
                          [user :as model.user])))

(deffilter #'delete :http
  [action request]
  (-> request :params :id show action))

(deffilter #'new :http
  [action request]
  (action))

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
