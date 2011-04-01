(ns jiksnu.http.view.subscription-view
  (:use [ciste.core :only (defview)]
        ciste.sections
        ciste.view
        jiksnu.http.controller.subscription-controller
        [ciste.html :only (dump dump*)]
        jiksnu.view)
  (:require [hiccup.form-helpers :as f]
            [jiksnu.http.view.user-view :as view.user]
            [jiksnu.model.user :as model.user])
  (:import jiksnu.model.Subscription))

(defview #'index :html
  [request subscriptions]
  {:body (index-section subscriptions)})

(defview #'subscribe :html
  [request subscription]
  {:status 302
   :template false
   :headers {"Location" "/"}})

(defview #'unsubscribe :html
  [request subscription]
  {:status 302
   :template false
   :headers {"Location" "/"}})

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
            [:td (view.user/avatar-img to)]
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

(defview #'subscribers :html
  [request subscribers]
  {:body [:div "subscribers"]})
