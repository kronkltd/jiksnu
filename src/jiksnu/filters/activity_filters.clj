(ns jiksnu.filters.activity-filters
  (:use [ciste.config :only [config]]
        [ciste.debug :only [spy]]
        [ciste.filters :only [deffilter]]
        jiksnu.actions.activity-actions)
  (:require [aleph.http :as http]
            [clj-tigase.core :as tigase]
            [clj-tigase.element :as element]
            [clojure.java.io :as io]
            [jiksnu.abdera :as abdera]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.model :as model]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.like :as model.like]
            [jiksnu.model.user :as model.user]
            [jiksnu.sections.activity-sections :as sections.activity])
  (:import tigase.xml.Element))

(deffilter #'delete :http
  [action request]
  (-> request :params :id model.activity/fetch-by-id action))

;; (deffilter #'new :http
;;   [action request]
;;   (action))

(deffilter #'post :http
  [action request]
  (-> request :params action))

(deffilter #'show :http
  [action request]
  (-> request :params :id
      model.activity/fetch-by-id action))

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
        ids (map #(.getAttribute ^Element % "id") items)
        id (first ids)]
    (action (model.activity/fetch-by-id id))))

(deffilter #'remote-create :xmpp
  [action request]
  (if (not= (:to request) (:from request))
    (let [packet (:packet request)
          ;; items (element/children packet "/message/event/items/item")
          items (map (comp first element/children) (:items request))]
      (action (map #(entry->activity
                     (abdera/parse-xml-string (str %)))
                   items)))))
