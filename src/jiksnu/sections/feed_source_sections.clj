(ns jiksnu.sections.feed-source-sections
  (:use [ciste.sections :only [defsection]]
        [ciste.sections.default :only [add-form delete-button show-section index-line index-block
                                       index-section link-to title update-button]]
        [clojurewerkz.route-one.core :only [named-path]]
        [jiksnu.ko :only [*dynamic*]]
        [jiksnu.sections :only [action-link actions-section admin-show-section admin-index-block admin-index-line admin-index-section control-line dump-data]])
  (:require [clojure.string :as string]
            [clojure.tools.logging :as log]
            [jiksnu.model.user :as model.user])
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
   [:h3 "Watchers "
    [:span (if *dynamic*
             {:data-bind "text: watchers.length"}
             (count (:watchers source)))]]
   [:div {:data-bind "with: watchers"}
    [:table.table
     [:tbody {:data-bind "foreach: $data"}
      (map
       (fn [id]
         (let [user (if *dynamic*
                      (User.)
                      (model.user/fetch-by-id id))]
           [:tr (merge
                 {:data-model "user"}
                 #_(if *dynamic*
                   {:data-bind
                    (string/join ", "
                                 ["if: $data"
                                  "attr: {'data-target': $data}"])}))
            [:td (link-to user)]
            [:td
             (action-link "feed-source" "remove-watcher" (:_id source)
                          {:target (:_id user)
                           :icon "trash"
                           :title "Delete"})]]))
       (if *dynamic*
         [""]
         (:watchers source)))]]]])

(defn add-watcher-form
  [source]
  [:form.well.form-horizontal
   {:method "post"
    :action (format "/admin/feed-sources/%s/watchers"
                    (:_id source))}
   [:fieldset
    [:legend "Add Watcher"]
    (control-line "Acct id"
                  :user_id "text")
    [:input {:type "submit"}]]])

(defn watch-button
  [item]
  (action-link "feed-source" "watch" (:_id item)))

(defn unwatch-button
  [item]
  (action-link "feed-source" "unwatch" (:_id item)))

;; actions-section

(defsection actions-section [FeedSource :html]
  [source]
  [:div.btn-group
   [:a.btn.dropdown-toggle {:data-toggle "dropdown"}
    [:span.caret]
    ]
   [:ul.dropdown-menu.pull-right
    [:li (update-button source)]
    [:li (subscribe-button source)]
    [:li [:a (if *dynamic*
               {:data-bind "attr: {href: '/model/feedSources/' + ko.utils.unwrapObservable(_id) + '.model'}"}
               {:href (format "/model/feedSources/%s.model" (:_id source))})
          "Model"]]
    [:li (unsubscribe-button source)]
    [:li (delete-button source)]
    [:li (watch-button source)]
    [:li (unwatch-button source)]]])

;; add-form

(defsection add-form [FeedSource :html]
  [source & options]
  [:form.well.form-horizontal {:method "post" :action "/admin/feed-sources"}
   [:fieldset
    [:legend "Add Source"]
    (control-line "Topic"  "topic" "text")
    (control-line "Callback" "callback" "text")
    (control-line "Challenge" "challenge" "text")
    ;; TODO: radio buttons?
    (control-line "Mode" "mode" "text")
    (control-line "User" "user" "text")
    
    [:div.form-actions
     [:button.btn.btn-primary
      {:type "submit"} "Add"]]]])

;; admin-index-block

(defsection admin-index-block [FeedSource]
  [items & [page]]
  (map #(admin-index-line % page) items))

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
   [:tbody (when *dynamic*
             {:data-bind "foreach: $data"})
    (map admin-index-line items)]])

(defsection admin-index-block [FeedSource :viewmodel]
  [items & [page]]
  (->> items
       (map (fn [m] (index-line m page)))
       doall))

;; admin-index-line

(defsection admin-index-line [FeedSource :html]
  [item & [page]]
  [:tr (merge {:data-model "feed-source"}
              (when-not *dynamic*
                {:data-id (:_id item)}))
   [:td
    (link-to item)]
   [:td
    [:a (if *dynamic*
          {:data-bind "attr: {href: '/admin/feed-sources/' + ko.utils.unwrapObservable(_id)}, text: title"}
          {:title (:title item)
           :href (named-path "admin show feed-source"
                             {:id (:_id item)})})
     (when-not *dynamic*
       (:title item))]]
   [:td (if *dynamic*
          {:data-bind "text: domain"}
          (:domain item))]

   [:td
    [:a (if *dynamic*
          {:data-bind "attr: {href: topic}, text: topic"}
          {:href (:topic item)})
     (when-not *dynamic* (:topic item))]]

   [:td (if *dynamic*
          {:data-bind "text: status"}
          (:status item))]

   [:td (actions-section item)]])

;; admin-index-section

(defsection admin-index-section [FeedSource]
  [items & [page]]
  (admin-index-block items page))

;; admin-show-section

(defsection admin-show-section [FeedSource]
  [item & [page]]
  (show-section item))

(defsection admin-show-section [FeedSource :html]
  [item & [page]]
  (show-section item))

;; delete-button

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
   [:tbody (map index-line sources)]])

(defsection index-block [FeedSource :viewmodel]
  [items & [page]]
  (->> items
       (map (fn [m] (index-line m page)))
       doall))

;; index-line

(defsection index-line [FeedSource :html]
  [source & _]
  [:tr {:data-model "feed-source"}
   [:td (:title source)]
   [:td (:domain source)]
   [:td (link-to source)]
   [:td (when (:hub source) "*")]
   #_[:td (:mode source)]
   [:td (str (:subscription-status source))]
   [:td (count (:watchers source))]
   [:td (:updated source)]
   [:td (actions-section source)]])

;; index-section

(defsection index-section [FeedSource :html]
  [sources & [options & _]]
  (index-block sources options))

;; link-to

(defsection link-to [FeedSource :html]
  [source & _]
  [:a (if *dynamic*
        {:data-bind "attr: {href: '/admin/feed-sources/' + ko.utils.unwrapObservable(_id)}, text: _id"}
        {:href (str "/admin/feed-sources/" (:_id source))})
   (:topic source)])

;; show-section

(defsection show-section [FeedSource :html]
  [source & options]
  (let [{:keys [topic callback challenge mode hub
                verify-token lease-seconds created updated]} source]
    [:div {:data-model "feed-source"}
     [:table.table
      [:tbody
       [:tr
        [:th "Topic:"]
        [:td
         [:a 
          (if *dynamic*
            {:data-bind "attr: {href: topic}, text: topic"}
            {:href topic})
          (when-not *dynamic*
            topic)]]]
       [:tr
        [:th "Hub:"]
        [:td [:a (if *dynamic*
                   {:data-bind "attr: {href: hub}, text: hub"}
                   {:href hub})
              (when-not *dynamic*
                hub)]]]
       [:tr
        [:th "Callback:"]
        [:td (merge {:data-property "callback"}
                    (when *dynamic*
                      {:data-bind "text: callback"}))
         (when-not *dynamic*
           callback)]]
       [:tr
        [:th  "Challenge:"]
        [:td (if *dynamic*
               {:data-bind "text: callback"}
               challenge)]]
       [:tr
        [:th "Mode:"]
        [:td (if *dynamic*
               {:data-bind "text: mode"}
               (or mode "unknown"))]]
       [:tr
        [:th "Status:"]
        [:td
         (if *dynamic*
           {:data-bind "text: status"}
           (:status source))]]
       [:tr
        [:th "Subscription Status:"]
        [:td (:subscription-status source)]]
       [:tr
        [:th "Verify Token:"]
        [:td verify-token]]
       [:tr
        [:th "Created:"]
        [:td (if *dynamic*
               {:data-bind "text: created"}
               created)]]
       [:tr
        [:th "Updated:"]
        [:td
         (if *dynamic*
           {:data-bind "text: updated"}
           updated)]]
       [:tr
        [:th "Lease Seconds:"]
        [:td lease-seconds]]]]
     (actions-section source)]))

(defsection show-section [FeedSource :model]
  [activity & [page]]
  activity)

(defsection show-section [FeedSource :viewmodel]
  [item & _]
  item)

;; title

(defsection title [FeedSource]
  [item & _]
  (:title item))

;; update-button

(defsection update-button [FeedSource :html]
  [item & _]
  (action-link "feed-source" "update" (:_id item)))
