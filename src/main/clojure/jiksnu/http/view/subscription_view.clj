(ns jiksnu.http.view.subscription-view
  (:use jiksnu.http.controller.subscription-controller
        [jiksnu.http.view :only (dump)]
        jiksnu.view
        [ciste.core :only (defview)]
        ciste.view)
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
    [:p (dump request)]
    [:p (dump arg)]

    ]
   }
  
  )
