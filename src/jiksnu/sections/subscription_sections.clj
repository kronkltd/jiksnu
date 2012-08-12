(ns jiksnu.sections.subscription-sections
  (:use [ciste.model :only [implement]]
        [ciste.sections :only [declare-section defsection]]
        [ciste.sections.default :only [delete-button full-uri uri title index-line
                                       index-block index-section link-to show-section]]
        [clojurewerkz.route-one.core :only [named-path]]
        [jiksnu.ko :only [*dynamic*]]
        [jiksnu.sections :only [control-line admin-index-block
                                admin-index-line admin-index-section]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.model.subscription :as model.subscription]
            [jiksnu.model.user :as model.user]
            [jiksnu.sections.user-sections :as sections.user])
  (:import jiksnu.model.Subscription
           jiksnu.model.User))

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
    (let [subscriptions (if *dynamic*
                          [(Subscription.)]
                          (model.subscription/subscribers user))]
      [:div.subscribers #_(when *dynamic*
                          {:data-bind "with: currentUser"})
       [:h3
        [:a
         (if *dynamic*
           {:data-bind "attr: {href: url + '/subscribers'}" :href "#"}
           {:href (named-path "user subscribers" {:id (:_id user)})})
         "Followers"] " "
        [:span
         (if *dynamic*
           {:data-bind "text: $root.subscriptions().length"}
           (count subscriptions))]]
       [:ul.unstyled
        (map (fn [subscription]
               [:li (sections.user/display-avatar (if *dynamic*
                                                    (User.)
                                                    (model.subscription/get-actor (log/spy subscription))) "24")]
               )
             (log/spy subscriptions))]])))

(defn subscriptions-widget
  [user]
  (when user
    (let [subscriptions (model.subscription/subscriptions user)]
      [:div.subscriptions
       [:h3
        [:a {:href (str (full-uri user) "/subscriptions")} "Following"] " " (count subscriptions)]
       [:ul (map (fn [subscription]
                   [:li (sections.user/display-avatar (if *dynamic*
                                                        (User.)
                                                        (model.subscription/get-target subscription)) "24")]) subscriptions)]
       [:p
        [:a {:href "/main/ostatussub"} "Add Remote"]]])))

;; admin-index-line

(defsection admin-index-line [Subscription :html]
  [subscription & [options & _]]
  [:tr {:data-type "subscription" :data-id (str (:_id subscription))}
   [:td (link-to subscription)]
   [:td (if-let [user (model.subscription/get-actor subscription)]
          (link-to user)
          "unknown")]
   [:td (if-let [user (model.subscription/get-target subscription)]
          (link-to user)
          "unknown")]
   [:td (:created subscription)]
   [:td (:pending subscription)]
   [:td (:local subscription)]
   [:td (delete-button subscription)]])

(defsection admin-index-line [Subscription :viewmodel]
  [item & [page]]
  item)

;; admin-index-block

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

(defsection admin-index-block [Subscription :viewmodel]
  [items & [page]]
  (->> items
       (map (fn [m] {(:_id m) (admin-index-line m page)}))
       (into {})))

;; index-block

(defsection index-block [Subscription :viewmodel]
  [items & [page]]
  (->> items
       (map (fn [m] {(:_id m) (index-line m page)}))
       (into {})))



;; index-line

(defsection index-line [Subscription :as]
  [subscription & _]
  (let [actor (model.subscription/get-actor subscription)
        target (model.subscription/get-target subscription)]
    {:verb "follow"
     :actor (show-section actor)
     :target (show-section target)}))

(defsection index-line [Subscription :viewmodel]
  [item & [page]]
  item)

;; index-section

(defsection index-section [Subscription :viewmodel]
  [items & [page]]
  (index-block items page))

(defsection subscriptions-line [Subscription :html]
  [item & [options & _]]
  [:li.subscription
   (merge {:data-id (:_id item) :data-type "subscription"})
   (if-let [user (if *dynamic*
                   (User.)
                   (model.subscription/get-target item))]
     (show-section user)
     "unknown")])

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
   (if-let [user (model.subscription/get-actor item)]
     (show-section user)
     "unknown")])

(defsection subscribers-block [Subscription :html]
  [items & [options & _]]
  [:ul.subscriptions
   (map (fn [item] (subscribers-line item options)) items)])

(defsection subscribers-section [Subscription :html]
  [items & [options & _]]
  (subscribers-block items options))


(defsection uri [Subscription]
  [subscription & _]
  (str "/admin/subscriptions/" (:_id subscription)))

