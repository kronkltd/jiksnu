(ns jiksnu.filters.comment-filters
  (:use [ciste.filters :only [deffilter]]
        jiksnu.actions.comment-actions)
  (:require [clj-tigase.core :as tigase]
            [clj-tigase.element :as element]
            [jiksnu.modules.atom.util :as abdera]
            [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.model.activity :as model.activity]))

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
    (if-let [activity (model.activity/fetch-by-id id)]
      (action activity))))

(deffilter #'fetch-comments-remote :xmpp
  [action request])

