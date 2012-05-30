(ns jiksnu.sections.setting-sections
  (:use [ciste.config :only [config]]
        [jiksnu.views :only [control-line]]))

(defn edit-form
  []
  [:form.well.form-horizontal
   {:method "post" :action "/admin/settings"}
   [:fieldset
    [:legend "Settings Page"]
    (control-line "Site Name"
                  "site.name" "text"
                  :value (config :site :name))
    (control-line "Domain"
                  "domain" "text"
                  :value (config :domain))
    (control-line "Admin Email"
                  "site.email" "text"
                  :value (config :site :email))
    (control-line "Print Actions"
                  "print.actions" "checkbox"
                  :checked (config :print :actions))
    (control-line "Print Request"
                  "print.request" "checkbox"
                  :checked (config :print :request))
    (control-line "Print Routes"
                  "print.routes" "checkbox"
                  :checked (config :print :routes))
    (control-line "Print Triggers"
                  "print.triggers" "checkbox"
                  :checked (config :print :triggers))
    (control-line "Allow registration?"
                  "registration-enabled" "checkbox"
                  :checked (config :registration-enabled))
    [:div.actions
     [:input {:type "submit"}]]]])
