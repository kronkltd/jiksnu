(ns jiksnu.sections.feed-source-sections
  (:use (ciste [sections :only [defsection]])
        (ciste.sections [default :only [add-form show-section]]))
  (:import jiksnu.model.FeedSource))

(defn control-line
  [label name type & options]
  [:div.control-group
   [:label.control-label {:for name} label]
   [:div.controls
    [:input {:type "text" :name name}]]])


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
                verify-token lease-seconds]} source]
    [:div
     [:p "Topic: " topic]
     [:p "Callback: " callback]
     [:p "Challenge: " challenge]
     [:p "Mode: " mode]
     [:p "Verify Token: " verify-token]
     [:p "Lease Seconds: " lease-seconds]]))
