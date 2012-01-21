(ns jiksnu.views.setting-views
  (:use ciste.views
        jiksnu.actions.setting-actions)
  (:require [hiccup.form-helpers :as f]
            (jiksnu.templates [setting :as templates.setting])))

(defview #'admin-edit-page :html
  [request _]
  {:body (templates.setting/edit-page)})

(defview #'avatar-page :html
  [request {:keys [user]}]
  {:body
   [:section
    [:h1 "Avatar"]
    [:form {:method "post" :action "/setting/avatar"}
     [:fieldset
      [:legend "Upload Avatar"]
      [:div.clearfix
       [:label {:for "avatar"} "Image"]
       [:div.input
        [:input {:type "file" :name "avatar"}]]]
      [:div.actions
       [:input {:type "submit" :value "Submit"}]]]]]})
