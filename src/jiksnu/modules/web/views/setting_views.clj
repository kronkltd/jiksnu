(ns jiksnu.modules.web.views.setting-views
  (:require [ciste.views :refer [defview]]
            [jiksnu.actions.setting-actions :as actions.setting]))

(defview #'actions.setting/avatar-page :html
  [request {:keys [user]}]
  {:title "Avatar"
   :body
   [:form {:method "post" :action "/settings/avatar"}
    [:fieldset
     [:legend "Upload Avatar"]
     [:div.clearfix
      [:label {:for "avatar"} "Image"]
      [:div.input
       [:input {:type "file" :name "avatar"}]]]
     [:div.actions
      [:input {:type "submit" :value "Submit"}]]]]}

  )
