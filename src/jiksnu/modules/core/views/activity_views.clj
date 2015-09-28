(ns jiksnu.modules.core.views.activity-views
  (:require [ciste.views :refer [defview]]
            [ciste.sections.default :refer [index-section show-section]]
            [clojure.tools.logging :as log]
            [jiksnu.actions.activity-actions :as actions.activity]))

(defview #'actions.activity/delete :model
  [request activity]
  {:body (show-section activity)})

(defview #'actions.activity/fetch-by-conversation :page
  [request response]
  (let [items (:items response)
        response (merge response
                        {:id (:name request)
                         :items (map :_id items)})]
    {:body {:action "sub-page-updated"
            :model "conversation"
            :id (:_id (:item request))
            :body response}}))

(defview #'actions.activity/index :page
  [request response]
  (let [items (:items response)
        response (merge response
                        {:id (:name request)
                         :items (map :_id items)})]
    {:body {:action "page-updated"
            :body response}}))

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

;; show

(defview #'actions.activity/show :clj
  [request activity]
  {:body activity})

(defview #'actions.activity/show :model
  [request activity]
  {:body (show-section activity)})
