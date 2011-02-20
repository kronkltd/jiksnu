(ns jiksnu.xmpp.controller.activity-controller
  (:use jiksnu.model
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
    (model.activity/show id)))

(defn index
  [request]
  (let [to (model.user/get-id (:to request))
        user (model.user/show to)]
    (model.activity/find-by-user user)))

(defn create-activity
  [item]
  #_(println "item: " item)
  (let [entry-string (str (first (children item)))
        ]
    #_(println "entry string: " entry-string)
    (let [entry (atom.view/parse-xml-string entry-string)
          ]
      #_(println "entry: " entry)
      (let [activity (atom.view.activity/to-activity entry)]
        (println "activity: " activity)
        (model.activity/create (make Activity activity))))))

(defn create
  [{:keys [items] :as  request}]
  #_(println "creating from: " request)

  (first (map create-activity items)))
