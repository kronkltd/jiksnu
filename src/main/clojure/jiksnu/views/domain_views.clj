(ns jiksnu.views.domain-views
  (:use ciste.core
        ciste.sections
        ciste.view
        jiksnu.http.controller.domain-controller
        jiksnu.model
        jiksnu.session
        jiksnu.view)
  (:require [hiccup.form-helpers :as f])
  (:import jiksnu.model.Domain))

(defview #'index :html
  [request domains]
  {:body (index-block domains)})


(defview #'show :html
  [request domain]
  {:body
   [:div
    [:p (:_id domain)]
    [:p (:osw domain)]
    [:p
     (f/form-to
      [:post (str "/domains/" (:_id domain) "/discover")]
      (f/submit-button "Discover"))]]})

(defview #'create :html
  [request domain]
  {:status 303
   :template false
   :headers {"Location" "/domains"}})

(defview #'discover :html
  [request domain]
  {:body "discovering"})

(defview #'delete :html
  [request domain]
  {:status 303
   :template false
   :headers {"Location" "/domains"}})
