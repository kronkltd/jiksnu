(ns jiksnu.modules.web.sections.conversation-sections
  (:use [ciste.sections :only [defsection]]
        [ciste.sections.default :only [actions-section delete-button title
                                       index-line index-block index-line link-to
                                       show-section update-button]]
        [jiksnu.modules.core.sections :only [admin-index-block admin-index-line]]
        [jiksnu.modules.web.sections :only [action-link bind-to control-line
                                            dropdown-menu pagination-links
                                            with-sub-page]])
  (:require [ciste.model :as cm]
            [clojure.tools.logging :as log]
            [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.conversation :as model.conversation]
            [jiksnu.model.domain :as model.domain]
            [jiksnu.model.feed-source :as model.feed-source]
            [jiksnu.namespace :as ns]
            [jiksnu.modules.web.sections.user-sections :as sections.user]
            [jiksnu.session :as session])
  (:import jiksnu.model.Activity
           jiksnu.model.Conversation
           jiksnu.model.Domain
           jiksnu.model.FeedSource
           jiksnu.model.User))

(defn discover-button
  [item]
  (action-link "conversation" "discover" (:_id item)))

(defn model-button
  [item]
  [:a {:href "/model/conversations/{{conversation.id}}.model"}
   "Model"])

(defn subscribe-button
  [item]
  (action-link "conversation" "subscribe" (:_id item)))

(defn unsubscribe-button
  [item]
  (action-link "conversation" "unsubscribe" (:_id item)))

(defn get-buttons
  []
  (concat
   (when (session/current-user)
     [#'subscribe-button
      #'discover-button
      #'model-button
      #'update-button])
   (when (session/is-admin?)
     [#'delete-button])))

;; actions-section

(defsection actions-section [Conversation :html]
  [item]
  (dropdown-menu item (get-buttons)))

(defsection admin-index-block [Conversation :html]
  [items & [page]]
  [:table.table
   [:thead
    [:tr
     [:th "Id"]
     [:th "Domain"]
     [:th "Url"]
     [:th "Parent"]
     [:th "Item Count"]
     #_[:th "Created"]
     [:th "Last Updated"]
     [:th "Record Updated"]
     [:th #_"Actions"]]]
   [:tbody
    (let [item (first items)]
      [:tr {:data-model "conversation"
            :ng-repeat "conversation in page.items"}
       [:td (link-to item)]
       [:td
        (let [domain (Domain.)]
          (bind-to "domain"
                   [:div {:data-model "domain"}
                    (link-to domain)]))]
       [:td
        [:a {:href "{{conversation.url}}"}
         "{{conversation.url}}"]]
       [:td "{{conversation.parent}"]
       [:td "{{conversation.itemCount}}"]
       ;; [:td "{{conversation.created}}"]
       [:td "{{conversation.lastUpdated}}"]
       [:td "{{conversation.updated}}"]
       [:td (actions-section item)]])]])

(defsection delete-button [Conversation :html]
  [user & _]
  (action-link "conversation" "delete" (:_id user)))

(defsection link-to [Conversation :html]
  [item & options]
  (let [options-map (apply hash-map options)]
    [:a {:href "/main/conversations/{{conversation.id}}"}
     [:span {:property "dc:title"
             :about "{{conversatio.uri}}"}
      "{{conversation.id}}"]]))

(defsection index-block [Conversation :html]
  [items & [page]]
  [:div
   [:a#showComments.btn {:ng-click "showComments"}
    "Show Comments"]
   (pagination-links page)
   [:div.conversations
    (let [item (first items)]
      (index-line item))]])

(defn show-details
  [item & [page]]
  (list
   (actions-section item)
   [:table.table
    [:tbody
     [:tr
      [:th "Domain"]
      [:td
       (if-let [domain (Domain.)]
         (bind-to "domain"
                  [:div {:data-model "domain"}
                   (link-to domain)]))]]
     [:tr
      [:th "Url"]
      [:td
       [:a {:href "{{conversation.url}}"}
        "{{conversation.url}}"]]]
     [:tr
      [:th "Item Count"]
      [:td "{{conversation.itemCount}}"]]
     [:tr
      [:th "Created"]
      [:td "{{conversation.created}}"]]
     [:tr
      [:th "Updated"]
      [:td "{{conversation.updated}}"]]
     [:tr
      [:th "Last Updated"]
      [:td "{{conversation.lastUpdated}}"]]
     [:tr
      [:th "Source"]
      [:td
       (let [source (FeedSource.)]
         (bind-to "$data['update-source']"
                  [:div {:data-model "feed-source"}
                   (link-to source)]))]]]]))

(defsection show-section [Conversation :html]
  [item & [page]]
  (let [items [(Activity.) (Activity.)]
        parent (first items)
        comments (next items)
        activity (first comments)
        author (User.)]
    [:div.conversation-section
     {:typeof "sioc:Container"
      :data-model "conversation"
      :ng-repeat "conversation in page.items"
      :about "{{conversation.url}}"
      :data-id "{{conversation.id}}"}
     ;; (show-details item)
     (list
      [:p "parent id: "
       [:a {:ui-sref "showActivity({id: conversation.parent })"
            }
        "{{conversation.parent}}"
        ]
       ]
      [:show-activity {:data-id "{{conversation.parent}}"}]
      #_(bind-to "$data.parent"
               (show-section parent))
      #_[:div {:data-bind "if: _view.showComments()"}
       (with-sub-page "activities"
         [:section.comments.clearfix
          [:div.comment {:data-model "activity"
                         :ng-repeat "comment in conversation.comments"
                         :data-id "{{comment.id}}"}
           (bind-to "author"
                    [:div {:data-model "user"}
                     [:a.pull-left
                      (sections.user/display-avatar author)]
                     (link-to author)])
           [:span.comment-content
            "{{comment.content}}"
            ]]])])]))

(defsection update-button [Conversation :html]
  [item & _]
  (action-link "conversation" "update" (:_id item)))
