(ns jiksnu.filters.comment-filters
  (:use [ciste.filters :only [deffilter]]
        jiksnu.actions.comment-actions)
  (:require [clj-tigase.core :as tigase]
            [clj-tigase.element :as element]
            [jiksnu.abdera :as abdera]
            [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.model.activity :as model.activity]))

(deffilter #'add-comment :http
  [action request]
  (-> request :params action))

(deffilter #'fetch-comments :http
  [action request]
  (-> request :params :id actions.activity/show action))

(deffilter #'new-comment :http
  [action request]
  (-> request :params :id model.activity/show))

(deffilter #'comment-response :xmpp
  [action request]
  (if (not= (:to request) (:from request))
    (let [packet (:packet request)
          items (:items request)]
      (action (map #(actions.activity/entry->activity
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

