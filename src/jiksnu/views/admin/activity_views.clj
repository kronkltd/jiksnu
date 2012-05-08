(ns jiksnu.views.admin.activity-views
  (:use (ciste [views :only [defview]])
        (ciste.sections [default :only [link-to]])
        jiksnu.actions.admin.activity-actions)
  (:require (jiksnu.actions [activity-actions :as actions.activity]))
  )


;; TODO: This page should use a single column
(defview #'index :html
  [request activities]
  {:title "Activities"
   :single true
   :body
   [:table.table
    [:thead
     [:tr
      [:th "user"]
      [:th "type"]
      [:th "visibility"]

      [:th "title"]]]
    [:tbody
     (map
      (fn [activity]
        [:tr
         [:td (-> activity actions.activity/get-author link-to)]
         [:td (-> activity :object :object-type)]
         [:td (if (-> activity :public) "public" "private")]
         [:td (:title activity)]])
      activities)]]})
