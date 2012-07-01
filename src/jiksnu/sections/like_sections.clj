(ns jiksnu.sections.like-sections
  (:use [ciste.sections :only [defsection]]
        [ciste.sections.default :only [actions-section index-line
                                       index-block index-section
                                       delete-button link-to]]
        [jiksnu.sections :only [admin-index-line admin-index-block admin-index-section]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.user :as model.user])
  (:import jiksnu.model.Like))

(defsection admin-index-block [Like :html]
  [likes & [options & _]]
  [:table.likes.table
   [:thead
    [:tr
     [:th]
     [:th "User"]
     [:th "Activity"]
     [:th "Updated"]
     [:th "Actions"]]]
   [:tbody
    (map index-line likes)]])

(defsection admin-index-line [Like :html]
  [like & _]
  [:tr {:data-id (:_id (log/spy like)) :data-type "like"}
   [:td (link-to (model.user/fetch-by-id (:user like)))]
   [:td (link-to (model.activity/fetch-by-id (:activity like)))]
   [:td (actions-section like)]])



(defsection actions-section [Like :html]
  [like & _]
  [:ul.buttons
   [:li (delete-button like)]])

(defsection index-line [Like :html]
  [like & _]
  [:tr
   [:td (link-to (model.user/fetch-by-id (:user like)))]
   [:td (link-to (model.activity/fetch-by-id (:activity like)))]
   [:td (actions-section like)]])

(defsection index-block [Like :html]
  [likes & _]
  [:table.users.table
   [:thead
    [:tr
     [:th]
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
