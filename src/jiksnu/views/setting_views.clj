(ns jiksnu.views.setting-views
  (:use (ciste [views :only [defview]])
        jiksnu.actions.setting-actions)
  (:require (hiccup [form :as f])
            (jiksnu.sections [setting-sections :as sections.setting])))

(defview #'admin-edit-page :html
  [request _]
  {:body (sections.setting/edit-form)})

(defview #'avatar-page :html
  [request {:keys [user]}]
  {:title "Avatar"
   :body
   [:form {:method "post" :action "/setting/avatar"}
    [:fieldset
     [:legend "Upload Avatar"]
     [:div.clearfix
      [:label {:for "avatar"} "Image"]
      [:div.input
       [:input {:type "file" :name "avatar"}]]]
     [:div.actions
      [:input {:type "submit" :value "Submit"}]]]]})

(defview #'config-output :json
  [request data]
  {:body data})

(defview #'update-settings :html
  [request data]
  {:flash "Settings updated"
   :status 303
   :template false
   :headers {"Location" "/admin/settings"}})
