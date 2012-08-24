(ns jiksnu.sections.like-sections
  (:use [ciste.sections :only [defsection]]
        [ciste.sections.default :only [actions-section index-line
                                       index-block index-section
                                       delete-button link-to]]
        [jiksnu.sections :only [admin-index-line admin-index-block
                                admin-index-section]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.user :as model.user])
  (:import jiksnu.model.Like))

(defsection actions-section [Like :html]
  [like & _]
  [:ul.buttons
   [:li (delete-button like)]])


(defsection admin-index-block [Like :html]
  [likes & [options & _]]
  [:table.likes.table
   [:thead
    [:tr
     [:th "User"]
     [:th "Activity"]
     [:th "Updated"]
     [:th "Actions"]]]
   [:tbody
    (map index-line likes)]])


(defsection admin-index-line [Like :html]
  [like & _]
  [:tr {:data-id (:_id like) :data-model "like"}
   [:td (link-to (model.user/fetch-by-id (:user like)))]
   [:td (link-to (model.activity/fetch-by-id (:activity like)))]
   [:td (:updated like)]
   [:td (actions-section like)]])


(defsection delete-button [Like :html]
  [record & _]
  [:form {:method "post"
          :action (format "/admin/likes/%s/delete" (:_id record))}
   [:button.btn.delete-button {:type "submit"}
    [:i.icon-trash] [:span.button-text "Delete"]]])

(defsection index-line [Like :html]
  [like & _]
  [:tr {:data-id (:_id like) :data-model "like"}
   [:td (link-to (model.user/fetch-by-id (:user like)))]
   [:td (link-to (model.activity/fetch-by-id (:activity like)))]
   [:td (:updated like)]
   [:td (actions-section like)]])


(defsection index-block [Like :html]
  [likes & _]
  [:table.users.table
   [:thead
    [:tr
     [:th "User"]
     [:th "Activity"]
     [:th "Updated"]
     [:th "Actions"]]]
   [:tbody
    (map index-line likes)]])


(defsection index-section [Like :html]
  [likes & _]
  [:div
   (index-block likes)])
