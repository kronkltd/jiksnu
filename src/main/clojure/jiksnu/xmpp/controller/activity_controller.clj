(ns jiksnu.xmpp.controller.activity-controller
  (:use clj-tigase.core
        jiksnu.core
        jiksnu.xmpp.element
        jiksnu.model
        jiksnu.namespace
        jiksnu.xmpp.view
        [karras.entity :only (make)])
  (:require [jiksnu.atom.view :as atom.view]
            [jiksnu.atom.view.activity-view :as atom.view.activity]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.user :as model.user])
  (:import org.apache.abdera.model.Entry
           jiksnu.model.Activity))

(defn show
  [{:keys [items] :as request}]
  (let [ids (map #(.getAttribute % "id") items)
        id (first ids)]
    (spy ids)
    (let [activity (model.activity/show id)]
      (spy activity)
      activity)))

(defn index
  [request]
  (let [to (model.user/get-id (:to request))
        user (model.user/show to)]
    (model.activity/find-by-user user)))

(defn create-activity
  [item]
  (let [entry-string (str (first (children item)))
        entry (atom.view/parse-xml-string entry-string)
        activity (atom.view.activity/to-activity entry)]
    (model.activity/create (make Activity activity))))

(defn create
  [{:keys [items] :as  request}]
  (let [activities (map create-activity items)]
    (first activities)))

(defn remote-create
  [request]
  (if (not= (:to request) (:from request))
    (let [packet (:packet request)
          items (children packet "/message/event/items/item")]
      (doseq [entry items]
        (let [activity (atom.view.activity/to-activity
                        (atom.view/parse-xml-string (str entry)))]
          (model.activity/create-raw activity)))
      true)))

(defn fetch-comments
  [{{id "id"} :params :as request}]
  (if-let [activity (model.activity/show id)]
    (map model.activity/show (:comments activity))))

(defn fetch-comments-remote
  [request]
  
  )
