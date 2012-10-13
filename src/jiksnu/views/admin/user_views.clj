(ns jiksnu.views.admin.user-views
  (:use [ciste.config :only [config]]
        [ciste.sections.default :only [title]]
        [ciste.views :only [defview]]
        [jiksnu.actions.admin.user-actions :only [index show]]
        [jiksnu.sections :only [admin-index-section admin-index-block admin-show-section]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.actions.stream-actions :as actions.stream]))

(defview #'index :html
  [request {:keys [items] :as response}]
  {:single true
   :title "Users"
   :body (admin-index-section items response)})

(defview #'index :viewmodel
  [request {:keys [items] :as response}]
  {:single true
   :body
   {:title "Users"
    :site {:name (config :site :name)}
    :showPostForm false
    :notifications []
    :pageInfo {:page (:page response)
               :totalRecords (:total-records response)
               :pageSize (:page-size response)
               :recordCount (count (:items response))}
    :users (doall (admin-index-section items))}})

(defview #'show :html
  [request user]
  (let [activity-map (second (actions.stream/user-timeline user))]
    {:title (title user)
     :single true
     :body
     (list (admin-show-section user)
           (admin-index-block (:items activity-map) activity-map))}))

