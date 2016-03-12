(ns jiksnu.modules.core.views.activity-views
  (:require [ciste.views :refer [defview]]
            [ciste.sections.default :refer [index-section show-section]]
            [jiksnu.actions.activity-actions :as actions.activity]))

(defview #'actions.activity/fetch-by-conversation :page
  [request response]
  {:body (merge
          response
          {:model "conversation"
           :name (:name request)
           :id (:_id (:item request))})})

(defview #'actions.activity/index :page
  [request response]
  {:body (merge
          response
          {:name (:name request)})})

(defview #'actions.activity/fetch-by-stream :page
  [request response]
  {:body (merge response
                {:name (:name request)})})

(defview #'actions.activity/fetch-by-user :page
  [request response]
  {:body (merge
          response
          {:name (:name request)})})

(defview #'actions.activity/oembed :xml
  [request m]
  {:status 200
   :body
   [:oembed
    [:version (:version m)]
    [:type (:type m)]
    [:provider_name (:provider_name m)]
    [:provider_url (:provider_url m)]
    [:title (:title m)]
    [:author_name (:author_name m)]
    [:author_url (:author_url m)]
    [:url (:url m)]
    [:html (:html m)]]})
