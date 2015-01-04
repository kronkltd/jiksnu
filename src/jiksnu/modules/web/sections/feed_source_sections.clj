(ns jiksnu.modules.web.sections.feed-source-sections
  (:require [ciste.sections :refer [defsection]]
            [ciste.sections.default :refer [actions-section delete-button show-section
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

(defsection delete-button [FeedSource :html]
  [item & _]
  (action-link "feed-source" "delete" (:_id item)))

(defsection link-to [FeedSource :html]
  [source & _]
  [:a {:href "/admin/feed-sources/{{source.id}}"}
   (:topic source)])

(defsection update-button [FeedSource :html]
  [item & _]
  (action-link "feed-source" "update" (:_id item)))
