(ns jiksnu.sections.feed-source-sections
  (:use (ciste [debug :only [spy]]
               [sections :only [defsection]])
        (ciste.sections [default :only [add-form show-section index-line index-section link-to]])
        (jiksnu [views :only [control-line]]))
  (:import jiksnu.model.FeedSource))

(defn unsubscribe-button
  [source]
  [:form {:method "post" :action (str "/admin/feed-sources/" (:_id source) "/unsubscribe")}
   [:button.btn.delete-button {:type "submit"}
    [:i.icon-trash] [:span.button-text "Unsubscribe"]]])

(defsection link-to [FeedSource :html]
  [source & _]
  [:a {:href (str "/admin/feed-sources/" (:_id source))}
   (:topic source)])

(defsection add-form [FeedSource :html]
  [source & options]
  [:form.well {:method "post" :action "/admin/feed-sources"}
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
  (let [{:keys [topic callback challenge mode
                verify-token lease-seconds created updated]} source]
    [:div
     [:p "Id: " (:_id source)]
     [:p "Topic: " topic]
     [:p "Callback: " callback]
     [:p "Challenge: " challenge]
     [:p "Mode: " (or mode "unknown")]
     [:p "Verify Token: " verify-token]
     [:p "Created: " created]
     [:p "Updated: " updated]
     [:p "Lease Seconds: " lease-seconds]]))

(defsection index-line [FeedSource :html]
  [source & _]
  [:tr
   [:td]
   [:td (link-to source)]
   [:td (:hub source)]
   [:td (:mode source)]
   [:td (str (:status source))]
   [:td (unsubscribe-button source)]])

(defsection index-section [FeedSource :html]
  [sources & _]
  [:table.table
   [:thead
    [:tr
     [:th]
     [:th "Topic"]
     [:th "Hub"]
     [:th "Mode"]
     [:th "Status"]
     [:th "Unsubscribe"]]]
   [:tbody (map index-line sources)]])
