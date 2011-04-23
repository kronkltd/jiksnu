(ns jiksnu.views.subscription-views
  (:use ciste.debug
        [ciste.html :only (dump dump*)]
        ciste.sections
        ciste.sections.default
        ciste.views
        clj-tigase.core
        jiksnu.actions.subscription-actions
        jiksnu.helpers.subscription-helpers
        jiksnu.helpers.user-helpers
        jiksnu.model
        jiksnu.namespace
        jiksnu.session
        jiksnu.sections.subscription-sections
        jiksnu.view)
  (:require [hiccup.form-helpers :as f]
            [jiksnu.model.subscription :as model.subscription]
            [jiksnu.model.user :as model.user])
  (:import jiksnu.model.Subscription
           tigase.xml.Element
           java.text.SimpleDateFormat))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; delete
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defview #'delete :html
  [request _]
  {:status 302
   :template false
   :headers {"Location" "/admin/subscriptions"}})

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; index
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defview #'index :html
  [request subscriptions]
  {:body (index-section subscriptions)})

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; ostatus
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defview #'ostatus :html
  [request arg]
  {:body
   [:div
    (dump* request)
    (dump* arg)]})

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; ostatussub
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defview #'ostatussub :html
  [request arg]
  {:body
   [:div
    (f/form-to
     [:post "/main/ostatussub"]
     [:p (f/text-field :profile )]
     (f/submit-button "Submit"))]})

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; ostatussub-submit
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defview #'ostatussub-submit :html
  [request subscription]
  {:status 303
   :headers {"Location" "/"}
   :flash "The request has been sent"
   :template false})

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; remote-subscribe-confirm
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defview #'remote-subscribe-confirm :xmpp
  [request _]
  nil)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; subscribe
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defview #'subscribe :html
  [request subscription]
  {:status 302
   :template false
   :headers {"Location" "/"}})

(defview #'subscribe :xmpp
  [request subscription]
  (result-packet request (subscription-response-element subscription)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; subscribed
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defview #'subscribed :xmpp
  [request subscription]
  (result-packet request (subscriptions-response [subscription])))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; subscribers
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defview #'subscribers :html
  [request subscribers]
  {:body [:div "subscribers"]})

(defview #'subscribers :xmpp
  [request subscribers]
  (result-packet request (subscribers-response subscribers)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; subscriptions
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defview #'subscriptions :html
  [request [user subscriptions]]
  {:title "Subscriptions"
   :formats [{:href (str (uri user) "/subscriptions.json")
              :label "JSON"
              :type "application/json"}]
   :body
   [:div
    [:table
     [:thead
      [:tr
       [:th "Avatar"]
       [:th "To"]
       [:th "Pending"]
       [:th "Created"]
       [:th "Resend"]
       [:th "Cancel"]]]
     [:tbody
      (map
       (fn [subscription]
         (let [to (model.user/fetch-by-id (:to subscription))]
           [:tr
            [:td (avatar-img to)]
            [:td (link-to to)]
            [:td (:pending subscription)]
            [:td (:created subscription)]
            [:td (f/form-to
                  [:post "/main/subscribe"]
                  (f/hidden-field :subscribeto (:_id to))
                  (f/submit-button "Subscribe"))]
            [:td (f/form-to
                  [:post "/main/unsubscribe"]
                  (f/hidden-field :unsubscribeto (:_id to))
                  (f/submit-button "Unsubscribe"))]]))
       subscriptions)]]]})

(defview #'subscriptions :xmpp
  [request [user subscriptions]]
  (result-packet request (subscriptions-response subscriptions)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Unsubscribe
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defview #'unsubscribe :html
  [request subscription]
  {:status 302
   :template false
   :headers {"Location" "/"}})

(defview #'unsubscribe :xmpp
  [request subscription]
  (result-packet request (subscriptions-response [subscription])))
