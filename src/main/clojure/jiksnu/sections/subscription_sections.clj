(ns jiksnu.sections.subscription-sections
  (:use ciste.html
        ciste.sections
        ciste.sections.default
        clj-tigase.core
        jiksnu.helpers.subscription-helpers
        jiksnu.model
        jiksnu.namespace
        jiksnu.view)
  (:require [hiccup.form-helpers :as f]
            [jiksnu.model.subscription :as model.subscription]
            [jiksnu.model.user :as model.user])
  (:import jiksnu.model.Subscription))

(declare-section subscriptions-line)
(declare-section subscribers-line)
(declare-section subscriptions-block :seq)
(declare-section subscribers-block :seq)
(declare-section subscriptions-section :seq)
(declare-section subscribers-section :seq)

(defsection uri [Subscription]
  [subscription & options]
  (str "/subscriptions/" (:_id subscription)))

(defsection title [Subscription]
  [subscription & options]
  (:to subscription))

(defsection index-line [Subscription :html]
  [subscription & options]
  [:tr
   [:td (link-to subscription)]
   [:td (link-to (model.user/fetch-by-id (:from subscription)))]
   [:td (link-to (model.user/fetch-by-id (:to subscription)))]
   [:td (:pending subscription)]
   [:td (:created subscription)]
   [:td (delete-form subscription)]])

(defsection subscriptions-line [Subscription :html]
  [subscription & options]
  [:li (show-section-minimal
        (jiksnu.model.user/fetch-by-id (:to subscription)))
   (if (:pending subscription) " (pending)")])

(defsection index-block [Subscription :html]
  [subscriptions & options]
  [:table
    [:thead
     [:tr
      [:td "Id"]
      [:td "From"]
      [:td "To"]
      [:td "Pending"]
      [:td "Created"]]]
    [:tbody
     (map index-line subscriptions)]])

(defsection subscriptions-block [Subscription :html]
  [subscriptions & _]
  [:ul
   (map subscriptions-line subscriptions)])

(defsection index-section [Subscription :html]
  [subscriptions & options]
  [:div
   [:h2 "Subscriptions"]
   (index-block subscriptions)])

(defsection subscriptions-section [Subscription :html]
  [subscriptions & _]
  [:div#subscriptions
   [:h3 [:a {:href (str #_(uri user) "/subscriptions") }
         "Subscriptions"]]
   (subscriptions-block subscriptions)
   [:p [:a {:href "/main/ostatussub"} "Add Remote"]]])

(defsection subscribers-section [Subscription :html]
  [subscriptions & _]
  [:div#subscribers
   [:h3 [:a {:href (str #_(uri user) "/subscribers")}
         "Subscribers"]]
   [:ul
    (map
     (fn [subscriber]
       [:li (show-section-minimal
             (jiksnu.model.user/fetch-by-id (:from subscriber)))])
     subscriptions)]])
