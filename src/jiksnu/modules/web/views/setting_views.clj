(ns jiksnu.modules.core.views.setting-views
  (:refer [ciste.views :refer [defview]]
          [jiksnu.actions.setting-actions :as actions.setting]
          [jiksnu.routes.helpers :refer [named-path]]))

(defview #'actions.setting/avatar-page :html
  [request {:keys [user]}]
  {:title "Avatar"
   :body
   [:form {:method "post" :action (named-path "avatar settings")}
    [:fieldset
     [:legend "Upload Avatar"]
     [:div.clearfix
      [:label {:for "avatar"} "Image"]
      [:div.input
       [:input {:type "file" :name "avatar"}]]]
     [:div.actions
      [:input {:type "submit" :value "Submit"}]]]]})
