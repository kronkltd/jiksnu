(ns jiksnu.sections.setting-sections)

(defn edit-form
  []
  [:form {:method "post" :action "/main/settings"}
   [:fieldset
    [:legend "Settings Page"]
    [:div.clearfix
     [:label {:for "site.name"} "Site Name"]
     [:div.input
      [:input {:type "text" :name "site.name"}]]]
    [:div.clearfix
     [:label {:for "print.actions"} "Print Actions"]
     [:div.input
      [:input {:type "checkbox" :name "print.actions"}]]]
    [:div.clearfix
     [:label {:for "print.request"} "Print Request"]
     [:div.input
      [:input {:type "checkbox" :name "print.request"}]]]
    [:div.clearfix
     [:label {:for "registration-enabled"} "Registration Enabled"]
     [:div.input
      [:input {:type "checkbox" :name "registration-enabled"}]]]
    [:div.actions
     [:input {:type "submit"}]]]])
