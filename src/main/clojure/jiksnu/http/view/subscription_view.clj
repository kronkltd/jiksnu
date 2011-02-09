(ns jiksnu.http.view.subscription-view
  (:use jiksnu.http.controller.subscription-controller
        [jiksnu.http.view :only (dump)]
        jiksnu.view
        [ciste.core :only (defview)]
        ciste.view)
  (:require [hiccup.form-helpers :as f])
  (:import jiksnu.model.Subscription))

(defmethod uri Subscription
  [subscription]
  (str "/subscriptions/" (:_id subscription)))

(defmethod title Subscription
  [subscription]
  (:to subscription))

(defn delete-form
  [subscription]
  (f/form-to [:delete (uri subscription)]
             (f/hidden-field :id (:_id subscription))
             (f/submit-button "Delete")))

(defmethod index-line Subscription
  [subscription]
  [:tr
   [:td [:a {:href "#"} (:_id subscription)]]
   [:td (:from subscription)]
   [:td (:to subscription)]
   [:td (:created subscription)]
   [:td (delete-form subscription)]])

(defmethod index-block Subscription
  [subscriptions]
  [:table
    [:thead
     [:tr
      [:td "Id"]
      [:td "From"]
      [:td "To"]
      [:td "Created"]]]
    [:tbody
     (map index-line subscriptions)]])

(defmethod index-section Subscription
  [subscriptions]
  [:div
   [:h2 "Subscriptions"]
   (index-block subscriptions)
   (dump subscriptions)])

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
