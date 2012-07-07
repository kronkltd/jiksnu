(ns jiksnu.sections.feed-source-sections
  (:use [ciste.sections :only [defsection]]
        [ciste.sections.default :only [add-form delete-button show-section index-line
                                       index-section link-to update-button]]
        [jiksnu.sections :only [actions-section control-line]])
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

(defsection link-to [FeedSource :html]
  [source & _]
  [:a {:href (str "/admin/feed-sources/" (:_id source))}
   (:topic source)])

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

(defsection show-section [FeedSource :html]
  [source & options]
  (let [{:keys [topic callback challenge mode hub
                verify-token lease-seconds created updated]} source]
    [:div
     [:p "Id: " (:_id source)]
     [:p "Title: " (:title source)]
     [:p "Topic: " topic]
     [:p "Hub: " hub]
     [:p "Callback: " callback]
     [:p "Challenge: " challenge]
     [:p "Mode: " (or mode "unknown")]
     [:p "Status: " (:status source)]
     [:p "Subscription Status: " (:subscription-status source)]
     [:p "Verify Token: " verify-token]
     [:p "Created: " created]
     [:p "Updated: " updated]
     [:p "Lease Seconds: " lease-seconds]
     (actions-section source)]))

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
   [:td
    (actions-section source)
    ;; (update-button source)
    ;; (subscribe-button source)
    ;; (unsubscribe-button source)
    ;; (delete-button source)
    ]])

(defsection index-section [FeedSource :html]
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

(defsection update-button [FeedSource :html]
  [activity & _]
  [:form {:method "post" :action (str "/admin/feed-sources/" (:_id activity) "/update")}
   [:button.btn.update-button {:type "submit"}
    [:i.icon-refresh] [:span.button-text "update"]]])

(defsection delete-button [FeedSource :html]
  [user & _]
  [:form {:method "post" :action (str "/admin/feed-sources/" (:_id user) "/delete")}
   [:button.btn.delete-button {:type "submit" :title "Delete"}
    [:i.icon-trash] [:span.button-text "Delete"]]])

(defn index-watchers
  [source]
  [:div.watchers
   [:h3 "Watchers " (count (:watchers source))]
   [:ul
    (map
     (fn [id]
       [:li
        (link-to
         (model.user/fetch-by-id id))])
     (:watchers source))]])

(defn add-watcher-form
  [source]
  [:form.well.form-horizontal
   [:fieldset
    [:legend "Add Watcher"]
    (control-line "Acct id"
                  :user_id "text")
    [:input {:type "submit"}]]])
