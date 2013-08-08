(ns jiksnu.modules.core.views.setting-views
  (:use [ciste.views :only [defview]]
        [clojurewerkz.route-one.core :only [named-path]]
        jiksnu.actions.setting-actions))

(defview #'avatar-page :html
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
