(ns jiksnu.xmpp.controller.activity-controller
  (:use jiksnu.model
        jiksnu.namespace
        jiksnu.xmpp.view
        [karras.entity :only (make)])
  (:require [jiksnu.atom.view :as atom.view]
            [jiksnu.atom.view.activity-view :as atom.view.activity]
            [jiksnu.model.activity :as model.activity])
  (:import org.apache.abdera.model.Entry
           jiksnu.model.Activity))

(defn show
  [{:keys [items] :as request}]
  (let [ids (map #(.getAttribute % "id") items)
        id (first ids)]
    (model.activity/show id)))

(defn index
  [items-node]
  (model.activity/index))

(defn create-activity
  [item]
  (let [entry-string (str (first (children item)))
        entry (atom.view/parse-xml-string entry-string)
        activity (atom.view.activity/to-activity entry)]
    (model.activity/create (make Activity activity))))

(defn create
  [{:keys [items] :as  request}]
  (first (map create-activity items)))
