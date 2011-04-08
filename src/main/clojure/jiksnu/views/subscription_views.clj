(ns jiksnu.views.subscription-views
  (:use [ciste.core :only (defview)]
        [ciste.html :only (dump dump*)]
        ciste.sections
        ciste.view
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
           java.text.SimpleDateFormat
           ))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; index
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defview #'index :html
  [request subscriptions]
  {:body (index-section subscriptions)})

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
;; Unsubscribe
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defview #'unsubscribe :html
  [request subscription]
  {:status 302
   :template false
   :headers {"Location" "/"}})

(defview #'unsubscribe :xmpp
  [request subscription]
  (result-packet request nil))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; subscriptions
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defview #'subscriptions :html
  [request subscriptions]
  {:title "Subscriptions"
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
  [request subscriptions]
  (result-packet request (subscription-response-minimal subscriptions)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; subscribers
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defview #'subscribers :html
  [request subscribers]
  {:body [:div "subscribers"]})

(defview #'subscribers :xmpp
  [request subscribers]
  (result-packet request (subscriber-response-minimal subscribers)))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; delete
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defview #'delete :html
  [request _]
  {:status 302
   :template false
   :headers {"Location" "/admin/subscriptions"}})
















(defview #'ostatus :html
  [request arg]
  {:body
   [:div
    (dump* request)
    (dump* arg)]})

(defview #'ostatussub :html
  [request arg]
  {:body
   [:div
    (f/form-to
     [:post "/main/ostatussub"]
     [:p (f/text-field :profile )]
     (f/submit-button "Submit"))
    #_(dump* request)
    #_(dump* arg)]})

(defview #'ostatussub-submit :html
  [request subscription]
  {:status 303
   :headers {"Location" "/"}
   :flash "The request has been sent"
   :template false})

(defview #'subscribed :xmpp
  [request subscription]
  (set-packet request subscription))

(defview #'remote-subscribe-confirm :xmpp
  [request _]
  nil)
