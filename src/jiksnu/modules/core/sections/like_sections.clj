(ns jiksnu.modules.core.sections.like-sections
  (:require [ciste.sections :refer [defsection]]
            [ciste.sections.default :refer [actions-section index-line index-block index-section link-to]]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.user :as model.user]
            [jiksnu.modules.core.sections :refer [admin-index-line admin-index-block]])
  (:import jiksnu.model.Like))

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
   [:tbody {:data-bind "foreach: items"}
    (map index-line likes)]])


(defsection index-section [Like :html]
  [likes & _]
  [:div
   (index-block likes)])
