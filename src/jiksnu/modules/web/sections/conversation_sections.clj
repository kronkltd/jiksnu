(ns jiksnu.modules.web.sections.conversation-sections
  (:use [ciste.sections :only [defsection]]
        [ciste.sections.default :only [actions-section delete-button full-uri uri title index-line
                                       index-block index-line link-to show-section update-button]]
        [jiksnu.ko :only [*dynamic*]]
        [jiksnu.modules.core.sections :only [admin-index-block admin-index-line]]
        [jiksnu.modules.web.sections :only [action-link bind-to control-line display-property
                                            display-timestamp dropdown-menu
                                            pagination-links with-page with-sub-page]])
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
  [:a (if *dynamic*
        {:data-bind "attr: {href: '/model/conversations/' + _id() + '.model'}"}
        {:href (format "/model/conversations/%s.model" (:_id item))})
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
    (when *dynamic* {:data-bind "foreach: items"})
    (let [item (first items)]
      (admin-index-line item page))]])

;; admin-index-line

(defsection admin-index-line [Conversation :html]
  [item & [page]]
  [:tr {:data-model "conversation"
        :ng-repeat "conversation in conversations"}
   [:td (link-to item)]
   [:td
    (let [domain (if *dynamic* (Domain.) (model.domain/fetch-by-id (:domain item)))]
      (bind-to "domain"
        [:div {:data-model "domain"}
         (link-to domain)]))]
   [:td
    [:a {:href "{{conversation.url}}"}
     "{{conversation.url}}"]]
   [:td (display-property item :parent)]
   [:td "{{conversation.itemCount}}"]
   ;; [:td (display-property item :created)]
   [:td (display-timestamp item :lastUpdated)]
   [:td (display-timestamp item :updated)]
   [:td (actions-section item)]])

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
  [:div {:ng-controller "conversationListCtrl"}
   [:a#showComments.btn {:href "#"} "Show Comments"]
   [:div.conversations
    (map index-line items)]])

(defn show-details
  [item & [page]]
  (list
   (actions-section item)
   [:table.table
    [:tbody
     [:tr
      [:th "Domain"]
      [:td
       (if-let [domain (if *dynamic*
                         (Domain.)
                         (if-let [domain-name (:domain item)]
                           (model.domain/fetch-by-id domain-name)))]
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
      [:td (display-timestamp item :lastUpdated)]]
     [:tr
      [:th "Source"]
      [:td
       (let [source (if *dynamic*
                      (FeedSource.)
                      (if-let [id (:update-source item)]
                        (model.feed-source/fetch-by-id id)))]
         (bind-to "$data['update-source']"
           [:div {:data-model "feed-source"} (link-to source)]))]]]]))

(defn show-comment
  [activity]
  (let [author (if *dynamic*
                 (User.)
                 (model.activity/get-author activity))]
    [:div.comment (merge {:data-model "activity"}
                         (when-not *dynamic*
                           {:data-id (:_id author)}))
     (bind-to "author"
       [:div {:data-model "user"}
        [:a.pull-left
         (sections.user/display-avatar author)]
        (link-to author)])
     [:span.comment-content
      (display-property activity "content")]]))

(defsection show-section [Conversation :html]
  [item & [page]]
  (let [about-uri (full-uri item)
        items (if *dynamic*
                [(Activity.) (Activity.)]
                (:items (actions.activity/fetch-by-conversation item)))]
    [:div.conversation-section
     {:typeof "sioc:Container"
      :data-model "conversation"
      :about "{{conversation.url}}"
      :data-id "{{conversation.id}}"}
     ;; (show-details item)
     (let [parent (first items)]
       (list
        (bind-to "$data.parent"
          (show-section parent))
        [:div
         (when *dynamic* {:data-bind "if: _view.showComments()"})
         (with-sub-page "activities"
           #_(if-let [item (first items)]
             (bind-to "items()[0]"
               (show-section item))
             [:p "The parent activity for this conversation could not be found"])
           (when-let [comments (next items)]
             [:section.comments.clearfix
              [:div
               (when *dynamic* {:data-bind "foreach: items().slice(1)"})
               (map show-comment comments)]]))]))]))

;; update-button

(defsection update-button [Conversation :html]
  [item & _]
  (action-link "conversation" "update" (:_id item)))
