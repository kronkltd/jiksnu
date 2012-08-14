(ns jiksnu.sections.feed-source-sections
  (:use [ciste.sections :only [defsection]]
        [ciste.sections.default :only [add-form delete-button show-section index-line index-block
                                       index-section link-to title update-button]]
        [clojurewerkz.route-one.core :only [named-path]]
        [jiksnu.ko :only [*dynamic*]]
        [jiksnu.sections :only [actions-section admin-show-section admin-index-block admin-index-line admin-index-section control-line]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.model.user :as model.user])
  (:import jiksnu.model.FeedSource))

(defn subscribe-button
  [source]
  [:form {:method "post" :action (str "/admin/feed-sources/" (:_id source) "/subscribe")}
   [:button.btn.subscribe-button {:type "submit"}
    [:i.icon-eye-open] [:span.button-text "Subscribe"]]])

(defn unsubscribe-button
  [source]
  [:form {:method "post" :action (str "/admin/feed-sources/" (:_id source) "/unsubscribe")}
   [:button.btn.unsubscribe-button {:type "submit"}
    [:i.icon-eye-close] [:span.button-text "Unsubscribe"]]])

(defn index-watchers
  [source]
  [:div.watchers
   [:h3 "Watchers " (count (:watchers source))]
   [:table.table
    (map
     (fn [id]
       (let [user (model.user/fetch-by-id id)]
         [:tr
          [:td (link-to user)]
          [:td
           [:form
            {:method "post" :action (format "/admin/feed-sources/%s/watchers/delete" (:_id source))}
            [:input {:type "hidden" :name "user_id" :value (:_id user)}]
            [:button.btn.delete-button {:type "submit"}
             [:i.icon-trash] [:span.button-text "Delete"]]]]]))
     (:watchers source))]])

(defn add-watcher-form
  [source]
  [:form.well.form-horizontal
   {:method "post" :action (format "/admin/feed-sources/%s/watchers" (:_id source))}
   [:fieldset
    [:legend "Add Watcher"]
    (control-line "Acct id"
                  :user_id "text")
    [:input {:type "submit"}]]])

;; actions-section

(defsection actions-section [FeedSource :html]
  [source]
  [:ul.feed-source-actions.buttons
   [:li
    (update-button source)]
   [:li
    (subscribe-button source)]
   [:li
    (unsubscribe-button source)]
   [:li
    (delete-button source)]])

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
     [:th "Title"]
     [:th "Domain"]
     [:th "Topic"]
     [:th "Status"]
     [:th "Actions"]]]
   [:tbody (if *dynamic* {:data-bind "foreach: items"})
    (map admin-index-line items)]])

(defsection admin-index-block [FeedSource :viewmodel]
  [items & [page]]
  (->> items
       (map (fn [m] {(:_id m) (admin-index-line m page)}))
       (into {})))

;; admin-index-line

(defsection admin-index-line [FeedSource :html]
  [item & [page]]
  [:tr (merge {:data-type "feed-source" :data-id (:_id item)}
              (if *dynamic*
                {:data-bind "with: $root.feedSources()[$data]"}))
   [:td
    [:a (if *dynamic*
          {:data-bind "attr: {href: '/admin/feed-sources/' + _id}, text: title"}
          {:title (:title item) :href (named-path "admin show feed-source" {:id (:_id item)})})
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
  item)

(defsection admin-show-section [FeedSource :html]
  [item & [page]]
  (show-section item))

;; delete-button

(defsection delete-button [FeedSource :html]
  [user & _]
  [:form {:method "post" :action (str "/admin/feed-sources/" (:_id user) "/delete")}
   [:button.btn.delete-button {:type "submit" :title "Delete"}
    [:i.icon-trash] [:span.button-text "Delete"]]])

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

(defsection index-line [FeedSource :html]
  [source & _]
  [:tr {:data-type "feed-source" :data-id (str (:_id source))}
   [:td (:title source)]
   [:td (:domain source)]
   [:td (link-to source)]
   [:td (when (:hub source) "*")]
   #_[:td (:mode source)]
   [:td (str (:subscription-status source))]
   [:td (count (:watchers source))]
   [:td (:updated source)]
   [:td (actions-section source)]])

(defsection index-section [FeedSource :html]
  [sources & [options & _]]
  (index-block sources options))

(defsection link-to [FeedSource :html]
  [source & _]
  [:a {:href (str "/admin/feed-sources/" (:_id source))}
   (:topic source)])

(defsection show-section [FeedSource :html]
  [source & options]
  (let [{:keys [topic callback challenge mode hub
                verify-token lease-seconds created updated]} source]
    (list [:table.table
           [:tbody 
            [:tr
             [:th "Topic: "]
             [:td [:a {:href topic} topic]]]
            [:tr
             [:th "Hub: "]
             [:td [:a {:href hub} hub]]]
            [:tr
             [:th "Callback: "]
             [:td callback]]
            [:tr
             [:th  "Challenge: "]
             [:td challenge]]
            [:tr
             [:th "Mode: "]
             [:td (or mode "unknown")]]
            [:tr
             [:th "Status: "]
             [:td (:status source)]]
            [:tr
             [:th "Subscription Status: "]
             [:td (:subscription-status source)]]
            [:tr
             [:th "Verify Token: "]
             [:td verify-token]]
            [:tr
             [:th "Created: "]
             [:td created]]
            [:tr
             [:th "Updated: "]
             [:td updated]]
            [:tr
             [:th "Lease Seconds: "]
             [:td lease-seconds]]]]
          (actions-section source))))

(defsection title [FeedSource]
  [item & _]
  (:title item))

(defsection update-button [FeedSource :html]
  [activity & _]
  [:form {:method "post" :action (str "/admin/feed-sources/" (:_id activity) "/update")}
   [:button.btn.update-button {:type "submit"}
    [:i.icon-refresh] [:span.button-text "update"]]])

