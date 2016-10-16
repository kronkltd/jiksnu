(ns jiksnu.modules.core.filters
  (:require [ciste.filters :refer [deffilter]]
            [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.actions.album-actions :as actions.album]
            [jiksnu.actions.conversation-actions :as actions.conversation]
            [jiksnu.actions.domain-actions :as actions.domain]
            [jiksnu.actions.feed-source-actions :as actions.feed-source]
            [jiksnu.actions.group-actions :as actions.group]
            [jiksnu.actions.group-membership-actions :as actions.group-membership]
            [jiksnu.actions.resource-actions :as actions.resource]
            [jiksnu.actions.stream-actions :as actions.stream]
            [jiksnu.actions.user-actions :as actions.user]))

(defn parse-page
  [request]
  {:page (or (some-> request :params :page Integer/parseInt) 1)})

(defn parse-sorting
  [request]
  (let [order-by (:orderBy (:params request))
        direction (if (= "desc" (:direction (:params request))) -1 1)]
    (when (and order-by direction)
      {:sort-clause {(keyword order-by) direction}})))

(deffilter #'actions.activity/fetch-by-conversation :page
  [action request]
  (when-let [conversation (:item request)]
    (action conversation)))

(deffilter #'actions.activity/fetch-by-stream :page
  [action request]
  (when-let [item (:item request)]
    (action item)))

(deffilter #'actions.activity/fetch-by-user :page
  [action request]
  (when-let [item (:item request)]
    (action item)))

(deffilter #'actions.activity/index :page
  [action request]
  (action))

(deffilter #'actions.album/index :page
  [action request]
  (action))

(deffilter #'actions.album/fetch-by-user :page
  [action request]
  (when-let [item (:item request)]
    (action item)))

(deffilter #'actions.conversation/index :page
  [action request]
  (action))

(deffilter #'actions.conversation/fetch-by-group :page
  [action request]
  (action))

(deffilter #'actions.domain/index :page
  [action request]
  (action))

(deffilter #'actions.feed-source/index :page
  [action request]
  (action))

(deffilter #'actions.group/index :page
  [action request]
  (action))

(deffilter #'actions.group/fetch-admins :page
  [action request]
  (when-let [item (:item request)]
    (action item)))

(deffilter #'actions.group/fetch-by-user :page
  [action request]
  (when-let [item (:item request)]
    (action item)))

(deffilter #'actions.group-membership/index :page
  [action request]
  (action))

(deffilter #'actions.group-membership/fetch-by-group :page
  [action request]
  (when-let [item (:item request)]
    (action item)))

(deffilter #'actions.group-membership/fetch-by-user :page
  [action request]
  (when-let [item (:item request)]
    (action item)))

(deffilter #'actions.resource/index :page
  [action request]
  (action))

(deffilter #'actions.stream/fetch-by-user :page
  [action request]
  (let [item (:item request)]
    (action item)))

(deffilter #'actions.stream/outbox :page
  [action request]
  (let [item (:item request)]
    (action item)))

(deffilter #'actions.stream/index :page
  [action request]
  ;; TODO: fetch user
  (action))

(deffilter #'actions.stream/public-timeline :page
  [action request]
  (action))

(deffilter #'actions.stream/user-timeline :page
  [action request]
  (let [item (:item request)]
    (action item)))

(deffilter #'actions.user/index :page
  [action request]
  (action))
