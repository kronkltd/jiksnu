(ns jiksnu.filters.activity-filters
  (:use [ciste.config :only [config]]
        [ciste.filters :only [deffilter]]
        jiksnu.actions.activity-actions
        [slingshot.slingshot :only [try+]])
  (:require [aleph.http :as http]
            [clj-tigase.core :as tigase]
            [clj-tigase.element :as element]
            [clojure.tools.logging :as log]
            [jiksnu.abdera :as abdera]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.model :as model]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.like :as model.like]
            [jiksnu.model.user :as model.user]
            [jiksnu.sections.activity-sections :as sections.activity]
            [jiksnu.util :as util]
            [lamina.trace :as trace])
  (:import tigase.xml.Element))

;; delete

(deffilter #'delete :command
  [action id]
  (let [item (model.activity/fetch-by-id (util/make-id id))]
    (action item)))

(deffilter #'delete :http
  [action request]
  (if-let [id (try+ (-> request :params :id util/make-id)
                    (catch RuntimeException ex
                      (trace/trace "errors:handled" ex)))]
    (if-let [activity (model.activity/fetch-by-id id)]
      (action activity))))

;; fetch-by-conversation

(deffilter #'fetch-by-conversation :page
  [action request]
  (log/spy request)
  (when-let [conversation nil]
    (action conversation)))

;; oembed

(deffilter #'oembed :http
  [action request]
  (let [url (get-in request [:params :url])]
    (if-let [activity (model.activity/fetch-by-remote-id url)]
      (action activity))))

;; post

(deffilter #'post :http
  [action request]
  (-> request :params
      (dissoc "geo.latitude")
      action))

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

;; remote-create

(deffilter #'remote-create :xmpp
  [action request]
  (if (not= (:to request) (:from request))
    (let [packet (:packet request)
          ;; items (element/children packet "/message/event/items/item")
          items (map (comp first element/children) (:items request))]
      (action (map #(entry->activity
                     (abdera/parse-xml-string (str %)))
                   items)))))

;; show

(deffilter #'show :http
  [action request]
  (-> request :params :id util/make-id
      model.activity/fetch-by-id action))

(deffilter #'show :xmpp
  [action request]
  (let [{:keys [items]} request
        ids (map #(.getAttribute ^Element % "id") items)
        id (first ids)]
    (action (model.activity/fetch-by-id id))))

;; update

(deffilter #'update :http
  [action request]
  (-> request :params action))
