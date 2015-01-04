(ns jiksnu.modules.web.sections.activity-sections
  (:require [ciste.core :refer [with-format]]
            [ciste.sections :refer [defsection]]
            [ciste.sections.default :refer [actions-section
                                            delete-button edit-button
                                            show-section-minimal
                                            show-section link-to uri title
                                            index-block
                                            index-line index-section update-button]]
            [clojure.core.incubator :refer [-?>]]
            [clojure.string :as string]
            [clojure.tools.logging :as log]
            [hiccup.core :as h]
            [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.actions.comment-actions :as actions.comment]
            [jiksnu.model :as model]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.conversation :as model.conversation]
            [jiksnu.model.like :as model.like]
            [jiksnu.model.resource :as model.resource]
            [jiksnu.model.user :as model.user]
            [jiksnu.modules.core.sections :refer [admin-index-line
                                                  admin-index-block
                                                  admin-index-section]]
            [jiksnu.modules.web.sections :refer [action-link bind-to
                                                 control-line
                                                 dropdown-menu
                                                 format-links pagination-links
                                                 with-sub-page]]
            [jiksnu.namespace :as ns]
            [jiksnu.modules.web.sections.user-sections :as sections.user]
            [jiksnu.session :as session]
            [jiksnu.util :as util]
            [slingshot.slingshot :refer [throw+]])
  (:import jiksnu.model.Activity
           jiksnu.model.Conversation
           jiksnu.model.Resource
           jiksnu.model.User))

(defn like-button
  [activity]
  (action-link "activity" "like" (:_id activity)
               {:title "Like"
                :icon "heart"}))

(declare posted-link-section)

(defn show-comment
  [activity]
  (let [author (User.)]
    [:div.comment {:data-model "activity"}
     [:p
      [:span {:data-bind "with: author"}
       [:span {:data-model "user"}
        (sections.user/display-avatar author)
        (link-to author)]]
      ": "
      [:span "{{activity.title}}"]]
     #_[:p (posted-link-section activity)]]))

;; specific sections

(defn pictures-section
  [activity]
  )

(defn tag-section
  [activity]
)

(defn location-section
  [activity]
)


(defn privacy-select
  [activity]
  )

;; move to model

(defn comment-button
  [activity]
  [:a {:href "#"}
   [:i.icon-comment]
   [:span.button-text "Comment"]])

(defn model-button
  [activity]
  [:a {:href "/model/activities/{{activity.id}}.model" }
   "Model"])

(defn get-buttons
  []
  (concat
   [#'model-button]
   (when (session/current-user)
     [#'like-button
      #'comment-button])
   (when (or #_(model.activity/author? activity user)
             (session/is-admin?))
     [#'edit-button
      #'delete-button])
   (when (session/is-admin?)
     [#'update-button])))

(defn links-section
  [activity]
  [:h3 "Links"]
  )

(defn maps-section
  [activity]

  (let [geo {}]
    [:div.map-section
     (bind-to "geo"
              #_[:img.map
                 {:alt ""
                  :src
                  ;; TODO: use urly to construct this
                  ;; TODO: Move this to cljs
                  (str "https://maps.googleapis.com/maps/api/staticmap?"
                       (string/join "&amp;"
                                    ["size=200x200"
                                     "zoom=11"
                                     "sensor=true"
                                     (str "markers=color:red|"
                                          (:latitude geo)
                                          ","
                                          (:longitude geo))]))}]
              [:p "Latitude: " [:span {:data-bind "text: latitude"}
                                (:latitude geo)]]
              [:p "Longitude: " [:span {:data-bind "text: longitude"}
                                 (:longitude geo)]])]))

(defn likes-section
  [activity]
  (when-let [likes [{}]]
    [:section.likes {:ng-if "likeCount > 0"}
     [:span "Liked by"]
     [:ul
      (map
       (fn [like]
         [:li
          "Person"
          #_(link-to (model.like/get-actor like))])
       likes)]]))

(defn tags-section
  [activity]
  [:div.tags {:ng-if "activity.tags.length > 0"}
   [:span "Tags: "]
   [:ul.tags
    [:li {:ng-repeat "tag in activity.tags"}
     [:a {:rel "tag"
          :href "/tags/{{tag}}"}
      "{{tag}}"]]]])

(defn enclosures-section
  [activity]
  [:ul.unstyled
   [:li {:data-model "resource"
         :ng-repeat "resource in resources"}
    [:div {:ng-if "resource.properties"}
     [:div {:data-bind "if: properties()['og:type'] === 'video'"}
      [:div.video-embed
       [:iframe
        {:frameborder "0"
         :allowfullscreen "allowfullscreen"
         :data-bind "attr: {src: properties()['og:video']}"}]]]]
    [:a {:rel "lightbox"
         :href "{{resource.url}}"}
     [:img.enclosure
      {:alt ""
       :ng-src "{{resource.url}}"}]]]])

(def post-sections
  [#'enclosures-section
   ;; #'links-section
   #'likes-section
   #'maps-section
   #'tags-section
   #'posted-link-section
   ])

(defsection actions-section [Activity :html]
  [item]
  (dropdown-menu item (get-buttons)))

(defsection admin-index-block [Activity :html]
  [activities & [options & _]]
  [:table.table
   [:thead
    [:tr
     [:th "User"]
     [:th "Content"]
     [:th "Actions"]]]
   [:tbody
    {:data-bind "foreach: items"}
    (map admin-index-line activities)]])

;; admin-index-line

(defsection admin-index-line [Activity :html]
  [activity & [options & _]]
  [:tr {:data-model "activity"
        :data-id "{{activity.id}}"}
   [:td
    (let [user (User.)]
      [:span {:data-model "user"}
       (link-to user)])]
   [:td "{{activity.content}}"]
   [:td (actions-section activity)]])

;; edit-button

(defsection edit-button [Activity :html]
  [activity & _]
  (action-link "activity" "edit" (:_id activity)))

;; delete-button

(defsection delete-button [Activity :html]
  [activity & _]
  (action-link "activity" "delete" (:_id activity)))

(defsection index-block [Activity :html]
  [records & [options & _]]
  [:div.activities
   {:data-bind "foreach: items"}
   (map #(index-line % options) records)])

(defsection index-line [Activity :html]
  [activity & [page]]
  (show-section activity page))

(defsection index-section [Activity :html]
  [items & [page]]
  (index-block items page))

(defsection update-button [Activity :html]
  [activity & _]
  (action-link "activity" "update" (:_id activity)))

