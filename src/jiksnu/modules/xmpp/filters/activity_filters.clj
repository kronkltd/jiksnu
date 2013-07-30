(ns jiksnu.modules.xmpp.filters.activity-filters
  (:use [ciste.config :only [config]]
        [ciste.filters :only [deffilter]]
        jiksnu.actions.activity-actions
        [slingshot.slingshot :only [try+]])
  (:require [aleph.http :as http]
            [clj-tigase.core :as tigase]
            [clj-tigase.element :as element]
            [clojure.tools.logging :as log]
            [jiksnu.modules.atom.util :as abdera]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.model :as model]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.conversation :as model.conversation]
            [jiksnu.model.like :as model.like]
            [jiksnu.model.user :as model.user]
            [jiksnu.sections.activity-sections :as sections.activity]
            [jiksnu.util :as util]
            [lamina.trace :as trace])
  (:import tigase.xml.Element))

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

(deffilter #'show :xmpp
  [action request]
  (let [{:keys [items]} request
        ids (map #(.getAttribute ^Element % "id") items)
        id (first ids)]
    (action (model.activity/fetch-by-id id))))

