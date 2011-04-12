(ns jiksnu.views.settings-views
  (:use ciste.core
        ciste.view
        jiksnu.actions.settings-actions)
  (:require [hiccup.form-helpers :as f]))

(defview #'edit :html
  [request _]
  {:body
   [:div
    [:h1 "Settings page"]
    (f/form-to
     [:post "/settings"]
     [:ul
      [:li (f/label :print.request "Print Request")
       (f/check-box :print.request true)]
      [:li (f/label :registration-enabled "Registration Enabled?")
       (f/check-box :registration-enabled true)]])]})
