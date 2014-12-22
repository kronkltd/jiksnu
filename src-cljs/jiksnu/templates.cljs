(ns jiksnu.templates
  (:require [hipo :as hipo :include-macros true]))

(defn control-line
  [label name type & {:as options}]
  (let [{:keys [value checked]} options]
    [:div.control-group
     [:label.control-label {:for name} label]
     [:div.controls
      [:input
       (merge {:type type :name name}
              (when value
                {:value value})
              (when checked
                {:checked "checked"}))]]]))

(def login-page
  [:div
   [:div
    [:form {:method "post" :action "/main/login"}
     [:fieldset
      [:legend "Login"]
      [:div.clearfix
       [:label {:for "username"} "Username"]
       [:div.input
        [:input {:type "text" :name "username"}]]]
      [:div.clearfix
       [:label {:for "password"} "Password"]
       [:div.input
        [:input {:type "password" :name "password"}]]]
      [:div.actions
       [:input.btn.primary {:type "submit" :value "Login"}]]]]]
   [:div
    [:form {:method "post" :action "/main/guest-login"}
     [:fieldset
      [:legend "Guest Login"]
      [:div.clearfix
       [:label {:for "webid"} "Web Id"]
       [:div.input
        [:input {:type "text" :name "webid"}]]]
      [:div.actions
       [:input.btn.primary {:type "submit" :value "Login"}]]]]]])

(def register-page
  [:form.well.form-horizontal.register-form
   {:method "post" :action "/main/register"}
   [:fieldset
    [:legend "Register"]
    (map
     (fn [[label field type]]
       (control-line label field type))
     [["Username"               "username"         "text"]
      ["Password"               "password"         "password"]
      ["Confirm Password"       "confirm-password" "password"]
      ["Email"                  "email"            "email"]
      ["Display Name"           "display-name"     "text"]
      ["Location"               "location"         "text"]
      ["I have checked the box" "accepted"         "checkbox"]])
    [:div.actions
     [:input.btn.primary {:type "submit" :value "Register"}]]]])
