(ns jiksnu.views.domain-views
  (:use ciste.config
        ciste.core
        ciste.debug
        ciste.sections
        ciste.sections.default
        ciste.views
        clj-tigase.core
        jiksnu.actions.domain-actions
        jiksnu.model
        jiksnu.session
        jiksnu.view)
  (:require [hiccup.form-helpers :as f]
            jiksnu.sections.domain-sections)
  (:import jiksnu.model.Domain))

(defview #'create :html
  [request domain]
  {:status 303
   :template false
   :headers {"Location" "/domains"}})

(defview #'delete :html
  [request domain]
  {:status 303
   :template false
   :headers {"Location" "/domains"}})

(defview #'discover :html
  [request domain]
  {:status 303
   :template false
   :headers {"Location" "/domains"}})

(defview #'index :html
  [request domains]
  {:body (index-block domains)})

(defview #'ping :xmpp
  [request domain]
  {:type :get
   :to (make-jid "" (:_id domain))
   :from (make-jid "" (-> (config) :domain))
   :body (make-element ["ping" {"xmlns" "urn:xmpp:ping"}])})

(defview #'ping-response :xmpp
  [request domain]
  #_{:status 303
   :template false
   :headers {"Location" "/domains"}})

(defview #'show :html
  [request domain]
  {:body
   [:div
    [:p "Id: " (:_id domain)]
    [:p "XMPP: "
     (let [xmpp (:xmpp domain)]
       (if (nil? xmpp)
         "Unknown"
         xmpp
         )
                   )]
    [:p "Discovered: " (:discovered domain)]
    [:div
     [:table
      [:thead
       [:tr
        [:th "Rel"]
        [:th "Href"]
        [:th "Template"]
        [:th "Type"]]]
      [:tbody
       (map
        (fn [link]
          [:tr
           [:td (:rel link)]
           [:td (:href link)]
           [:td (:template link)]
           [:td (:type link)]])
        (:links domain))]]]
    [:p
     (f/form-to
      [:post (str "/domains/" (:_id domain) "/discover")]
      (f/submit-button "Discover"))]]})

