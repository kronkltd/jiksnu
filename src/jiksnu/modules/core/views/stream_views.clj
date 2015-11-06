(ns jiksnu.modules.core.views.stream-views
  (:require [ciste.core :refer [with-format]]
            [ciste.views :refer [apply-view defview]]
            [ciste.sections.default :refer [index-block index-line
                                            index-section show-section
                                           title]]
            [jiksnu.actions.stream-actions :as actions.stream]
            [jiksnu.model :as model]
            [jiksnu.modules.core.sections :refer [format-page-info]]
            [jiksnu.namespace :as ns]))

;; direct-message-timeline

(defview #'actions.stream/direct-message-timeline :xml
  [request activities]
  {:body
   [:statuses {:type "array"}
    (map index-line (index-section activities))]})

(defview #'actions.stream/fetch-by-user :page
  [request response]
  (let [items (:items response)]
    {:body
     (merge response
            {:id (:name request)
             :target-model "user"
             :target (:_id (:item request))
             :items (map :_id items)})}))

(defview #'actions.stream/home-timeline :xml
  [request activities]
  {:body (index-section activities)})

(defview #'actions.stream/index :page
  [request response]
  (let [items (:items response)
        response (merge response
                        {:id (:name request)
                         :items (map :_id items)})]
    {:body {:action "page-updated"
            :body response}}))

;; mentions-timeline

(defview #'actions.stream/mentions-timeline :xml
  [request activities]
  {:body
   [:statuses {:type "array"} (map index-line (index-section activities))]})

;; public-timeline

(defview #'actions.stream/public-timeline :page
  [request response]
  (let [items (:items response)
        response (merge response
                        {:id (:name request)
                         :items (map :_id items)})]
    {:body {:action "page-updated"
            :title "Public Timeline"
            :body response}}))

(defview #'actions.stream/public-timeline :xml
  [request {:keys [items] :as page}]
  {:body (index-section items page)})

(defview #'actions.stream/user-timeline :model
  [request [user page]]
  {:body (show-section user)})

(defview #'actions.stream/user-timeline :page
  [request [user page]]
  (let [items (:items page)
        response (merge page
                        {:id (:name request)
                         :items (map :_id items)})]
    {:body {:action "sub-page-updated"
            :model "user"
            :id (:_id (:item request))
            :body response}}))

(defview #'actions.stream/outbox :page
  [request [user page]]
  (let [items (:items page)
        response (merge page
                        {:id (:name request)
                         :items items})]
    {:title (title user)
     :body {:action "sub-page-updated"
            :model "user"
            :user user
            :id (:_id (:item request))
            :body response}}))

(defview #'actions.stream/user-timeline :xml
  [request [user activities]]
  {:body (index-block activities)
   :template :false})
