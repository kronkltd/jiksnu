(ns jiksnu.http.view.domain-view
  (:use ciste.core
        ciste.sections
        ciste.view
        jiksnu.http.controller.domain-controller
        jiksnu.model
        jiksnu.session
        jiksnu.view)
  (:require [hiccup.form-helpers :as f])
  (:import jiksnu.model.Domain))

(defsection uri [Domain :html]
  [domain & options]
  (str "/domains/" (:_id domain)))

(defsection add-form [Domain :html]
  [domain & options]
  [:div
   [:p "Add Domain"]
   (f/form-to
    [:post "/domains"]
    [:p
     (f/label :domain "Domain")
     (f/text-field :domain (:_id domain))
     (f/submit-button "Add")])])

(defsection index-line [Domain :html]
  [domain & options]
  [:tr
   [:td
    [:a {:href (uri domain)} (:_id domain)]]
   [:td (:osw domain)]
   [:td
    [:a {:href (str "http://" (:_id domain)
                    "/.well-known/host-meta")} "Host-Meta"]]
   [:td (f/form-to
         [:post (str "/domains/" (:_id domain) "/discover")]
         (f/submit-button "Discover"))]])

(defsection index-section [Domain :html]
  [domains & options]
  [:table
   [:tr
    [:th "Name"]
    [:th "OSW Enabled?"]]
   (map index-line  domains)])

(defsection index-block [Domain :html]
  [domains & options]
  [:div
    [:p "index domains"]
   (index-section domains)
   (add-form (Domain.))])

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
  ;; (println "domain: " domain)
  {:status 303
   :template false
   :headers {"Location" (uri domain)}})

(defview #'discover :html
  [request domain]
  {:body "discovering"})
