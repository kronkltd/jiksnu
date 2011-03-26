(ns jiksnu.http.view.subscription-view
  (:use [ciste.core :only (defview)]
        ciste.sections
        ciste.view
        jiksnu.http.controller.subscription-controller
        [ciste.html :only (dump dump*)]
        jiksnu.view)
  (:require [hiccup.form-helpers :as f]
            [jiksnu.model.user :as model.user])
  (:import jiksnu.model.Subscription))

(defsection uri [Subscription]
  [subscription & options]
  (str "/admin/subscriptions/" (:_id subscription)))

(defsection title [Subscription]
  [subscription & options]
  (:to subscription))

(defn delete-form
  [subscription]
  (f/form-to [:delete (uri subscription)]
             (f/hidden-field :id (:_id subscription))
             (f/submit-button "Delete")))

(defsection index-line [Subscription :html]
  [subscription & options]
  [:tr
   [:td [:a {:href "#"} (:_id subscription)]]
   [:td (link-to (model.user/fetch-by-id (:from subscription)))]
   [:td (link-to (model.user/fetch-by-id (:to subscription)))]
   [:td (:created subscription)]
   [:td (delete-form subscription)]])

(defsection index-block [Subscription :html]
  [subscriptions & options]
  [:table
    [:thead
     [:tr
      [:td "Id"]
      [:td "From"]
      [:td "To"]
      [:td "Created"]]]
    [:tbody
     (map index-line subscriptions)]])

(defsection index-section [Subscription :html]
  [subscriptions & options]
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
    (dump* request)
    (dump* arg)]})

(defview #'ostatussub-submit :html
  [request subscription]
  {:status 303
   :headers {"Location" "/"}
   :flash "The request has been sent"
   :template false})
