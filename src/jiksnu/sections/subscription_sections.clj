(ns jiksnu.sections.subscription-sections
  (:use [ciste.model :only [implement]]
        [ciste.sections :only [declare-section defsection]]
        [ciste.sections.default :only [delete-button full-uri uri title index-line
                                       index-section link-to show-section]]
        [jiksnu.sections :only [control-line admin-index-block admin-index-line admin-index-section]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.model.subscription :as model.subscription]
            [jiksnu.model.user :as model.user]
            [jiksnu.sections.user-sections :as sections.user])
  (:import jiksnu.model.Subscription))

;; subscriptions where the user is the target
(declare-section subscribers-section :seq)
(declare-section subscribers-block :seq)
(declare-section subscribers-line)

;; subscriptions where the user is the actor
(declare-section subscriptions-section :seq)
(declare-section subscriptions-block :seq)
(declare-section subscriptions-line)

(defn ostatus-sub-form
  []
  [:form {:method "post"
          :action "/main/ostatussub"}
   (control-line "Username"
                 "profile" "text")
   [:div.actions
    [:input.btn.primary {:type "submit" :value "Submit"}]]])

(defn subscribers-widget
  [user]
  (when user
    (let [subscriptions (model.subscription/subscribers user)]
      [:div.subscribers
       [:h3
        ;; subscribers link
        [:a {:href (str (full-uri user) "/subscribers")} "Followers"] " " (count subscriptions)]
       [:ul.unstyled
        [:li (map subscribers-line subscriptions)]]])))

(defn subscriptions-widget
  [user]
  (when user
    (let [subscriptions (model.subscription/subscriptions user)]
      [:div.subscriptions
       [:h3
        [:a {:href (str (full-uri user) "/subscriptions")} "Following"] " " (count subscriptions)]
       [:ul (map subscriptions-line subscriptions)]
       [:p
        [:a {:href "/main/ostatussub"} "Add Remote"]]])))

(defsection admin-index-line [Subscription :html]
  [subscription & [options & _]]
  [:tr
   [:td (link-to subscription)]
   [:td (let [user (model.subscription/get-actor subscription)]
          (link-to user))]
   [:td (let [user (model.subscription/get-target subscription )]
          (link-to user))]
   [:td (:created subscription)]
   [:td (:pending subscription)]
   [:td (:local subscription)]
   [:td (delete-button subscription)]])

(defsection admin-index-block [Subscription :html]
  [items & [options & _]]
  [:table.table.subscriptions
   [:thead
    [:tr
     [:th "id"]
     [:th "actor"]
     [:th "target"]
     [:th "Created"]
     [:th "pending"]
     [:th "local"]
     [:th "Delete"]]]
   [:tbody
    (map #(admin-index-line % options) items)]])

(defsection subscriptions-line [Subscription :html]
  [item & [options & _]]
  [:li.subscription {:data-id (:_item item) :data-type "subscription"}
   (link-to (model.subscription/get-actor item))])

(defsection subscriptions-block [Subscription :html]
  [items & [options & _]]
  [:ul.subscriptions
   (map (fn [item] (subscriptions-line item options)) items)])

(defsection subscriptions-section [Subscription :html]
  [items & [options & _]]
  (subscriptions-block items options))


(defsection subscribers-line [Subscription :html]
  [item & [options & _]]
  [:li.subscription {:data-id (:_item item) :data-type "subscription"}
   (link-to (model.subscription/get-target item))])

(defsection subscribers-block [Subscription :html]
  [items & [options & _]]
  [:ul.subscriptions
   (map (fn [item] (subscribers-line item options)) items)])

(defsection subscribers-section [Subscription :html]
  [items & [options & _]]
  (subscribers-block items options))


(defsection index-line [Subscription :as]
  [subscription & _]
  (let [actor (model.subscription/get-actor subscription)
        target (model.subscription/get-target subscription)]
    {:verb "follow"
     :actor (show-section actor)
     :target (show-section target)}))

(defsection uri [Subscription]
  [subscription & _]
  (str "/admin/subscriptions/" (:_id subscription)))

