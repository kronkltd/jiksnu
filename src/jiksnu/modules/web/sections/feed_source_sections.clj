(ns jiksnu.modules.web.sections.feed-source-sections
  (:require [ciste.sections :refer [defsection]]
            [ciste.sections.default :refer [actions-section add-form delete-button show-section
                                            index-line index-block index-section link-to title
                                            update-button]]
            [clojure.string :as string]
            [clojure.tools.logging :as log]
            [jiksnu.model.user :as model.user]
            [jiksnu.modules.core.sections :refer [admin-show-section
                                                  admin-index-block admin-index-line
                                                  admin-index-section]]
            [jiksnu.modules.web.sections :refer [action-link bind-to control-line
                                                 dropdown-menu]]
            [jiksnu.session :as session])
  (:import jiksnu.model.FeedSource
           jiksnu.model.User))

(defn subscribe-button
  [item & _]
  (action-link "feed-source" "subscribe" (:_id item)))

(defn unsubscribe-button
  [item & _]
  (action-link "feed-source" "unsubscribe" (:_id item)))

(defn index-watchers
  [source]
  [:div.watchers
   [:h3 "Watchers {{source.watchers.length}}"]
   (bind-to "watchers"
     [:table.table
      [:tbody
       (let [user (User.)]
         [:tr {:data-model "user"
               :ng-repeat "watcher in source.watchers"}
          [:td (link-to user)]
          [:td
           (action-link "feed-source" "remove-watcher" (:_id source)
                        {:target (:_id user)
                         :icon "trash"
                         :title "Delete"})]])]])])

(defn watch-button
  [item]
  (action-link "feed-source" "watch" (:_id item)))

(defn unwatch-button
  [item]
  (action-link "feed-source" "unwatch" (:_id item)))

(defn model-button
  [item]
  [:a {:href "/model/feed-sources/{{source.id}}.model"}
   "Model"])

(defn get-buttons
  []
  (concat
   [#'model-button]
   (when (session/current-user)
     [#'update-button
      #'subscribe-button
      #'unsubscribe-button
      #'watch-button
      #'unwatch-button])
   (when (session/is-admin?)
     [#'delete-button])))

(defsection actions-section [FeedSource :html]
  [item]
  (dropdown-menu item (get-buttons)))

(defsection add-form [FeedSource :html]
  [source & options]
  )

(defsection admin-index-block [FeedSource :html]
  [items & [options & _]]
  [:table.table
   [:thead
    [:tr
     [:th "Id"]
     [:th "Title"]
     [:th "Domain"]
     [:th "Topic"]
     [:th "Status"]
     [:th "Actions"]]]
   [:tbody {:data-bind "foreach: items"}
    (let [item (first items)]
      [:tr {:data-model "feed-source"
            :data-id "{{source.id}}"
            :ng-repeat "source in page.items"}
       [:td (link-to item)]
       [:td
        [:a {:title "{{source.title}}"
             :href "/admin/feed-sources/{{source.id}}" }
         "{{source.title}}"]]
       [:td "{{source.domain}}"]
       [:td
        [:a {:href "{{source.topic}}"}
         "{{source.topic}}"]]
       [:td "{{source.status}}"]
       [:td (actions-section item)]])]])

(defsection admin-show-section [FeedSource :html]
  [item & [page]]
  (show-section item))

(defsection delete-button [FeedSource :html]
  [item & _]
  (action-link "feed-source" "delete" (:_id item)))

(defsection index-block [FeedSource :html]
  [sources & _]
  [:table.table.feed-sources
   [:thead
    [:tr
     [:th "Title"]
     [:th "Domain"]
     [:th "Topic"]
     [:th "Hub"]
     #_[:th "Mode"]
     [:th "Status"]
     [:th "Watchers"]
     [:th "Updated"]
     [:th "Actions"]]]
   [:tbody
    (let [source (first sources)]
      [:tr {:ng-repeat "source in page.items"}
       [:td (link-to source)]
       [:td "{{source.domain}}"]
       [:td
        [:a {:href "{{source.topic}}"}
         "{{source.topic}}"]]
       [:td "{{source.hub}}"]
       #_[:td "{{source.mode}}"]
       [:td "{{source.status}}"
        ]
       [:td "{{source.watchers.length}}"]
       [:td "{{source.updated}}"]
       [:td (actions-section source)]])

    (map index-line sources)]])

(defsection index-section [FeedSource :html]
  [sources & [page & _]]
  (pagination-links page)
  (index-block sources page))

(defsection link-to [FeedSource :html]
  [source & _]
  [:a {:href "/admin/feed-sources/{{source.id}}"}
   (:topic source)])

(defsection show-section [FeedSource :html]
  [source & options]
  [:div {:data-model "feed-source"}
   (actions-section source)
   [:table.table
    [:tbody
     [:tr
      [:th "Topic:"]
      [:td [:a {:href "{{source.topic}}"}
            "{{source.topic}}"]]]
     [:tr
      [:th "Domain:"]
      [:td "{{source.domain}}"]]
     [:tr
      [:th "Hub:"]
      [:td [:a {:href "{{hub}}"}
            "{{hub}}"]]]
     [:tr
      [:th "Callback:"]
      [:td "{{source.callback}}"]]
     [:tr
      [:th  "Challenge:"]
      [:td "{{source.challenge}}"]]
     [:tr
      [:th "Mode:"]
      [:td "{{source.mode}}"]]
     [:tr
      [:th "Status:"]
      [:td "{{source.status}}"]]
     [:tr
      [:th "Verify Token:"]
      [:td "{{source.verifyToken}}"]]
     [:tr
      [:th "Created:"]
      [:td "{{source.created}}"]]
     [:tr
      [:th "Updated:"]
      [:td "{{source.updated}}"]]
     [:tr
      [:th "Lease Seconds:"]
      [:td "{{source.leaseSeconds}}"]]]]])

(defsection update-button [FeedSource :html]
  [item & _]
  (action-link "feed-source" "update" (:_id item)))
