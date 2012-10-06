(ns jiksnu.sections.subscription-sections
  (:use [ciste.model :only [implement]]
        [ciste.sections :only [declare-section defsection]]
        [ciste.sections.default :only [delete-button full-uri uri title index-line
                                edit-button         index-block index-section link-to show-section]]
        [clojurewerkz.route-one.core :only [named-path]]
        [jiksnu.ko :only [*dynamic*]]
        [jiksnu.sections :only [control-line admin-index-block dump-data
                                action-link admin-index-line admin-index-section]])
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

(defn actions-section
  [subscription]
  [:ul
   [:li (delete-button subscription)]])


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
      [:div.subscribers
       [:h3
        [:a (if *dynamic*
              {:data-bind "attr: {href: '/users/' + ko.utils.unwrapObservable(_id) + '/subscribers'}"}
              {:href (named-path "user subscribers" {:id (:_id user)})}) "Followers"]
        " "
        [:span (if *dynamic*
                 {:data-bind "text: $root.followers().length"}
                 (count subscriptions))]]
       [:ul.unstyled
        (if *dynamic* {:data-bind "foreach: $root.followers"})
        (map (fn [subscription]
               [:li {:data-model "subscription"}
                (let [user (if *dynamic*
                             (User.)
                             (model.subscription/get-actor subscription))]
                  (sections.user/display-avatar user "24"))])
             subscriptions)]])))

(defn subscriptions-widget
  [user]
  (when user
    (let [subscriptions (model.subscription/subscriptions user)]
      [:div.subscriptions
       [:h3
        [:a (if *dynamic*
              {:data-bind "attr: {href: '/users/' + ko.utils.unwrapObservable(_id) + '/subscriptions'}"}
              {:href (str (full-uri user) "/subscriptions")}) "Following"]
        " "
        [:span (if *dynamic*
                 {:data-bind "text: $root.following().length"}
                 (count subscriptions))]]
       [:div (if *dynamic* {:data-bind "with: $root.subscriptions()"})
        [:ul (if *dynamic* {:data-bind "foreach: $data"})
         (map (fn [subscription]
                [:li {:data-model "subscription"}
                 [:div {:data-bind "with: target"}
                  [:div {:data-model "user"}
                   (let [user (if *dynamic* (User.) (model.subscription/get-target subscription))]
                     (sections.user/display-avatar user "24"))]]]) subscriptions)]]
       [:p
        [:a {:href "/main/ostatussub"} "Add Remote"]]])))

;; admin-index-line

(defsection admin-index-line [Subscription :html]
  [subscription & [options & _]]
  [:tr (merge {:data-model "subscription"}
              (when-not *dynamic*
                {:data-id (str (:_id subscription))}))
   [:td
    (link-to subscription)]
   [:td
    [:div {:data-bind "with: from"}
     [:div {:data-model "user"}
      (if-let [user (if *dynamic*
                      (User.)
                      (model.subscription/get-actor subscription))]
        (link-to user)
        "unknown")]]]
   [:td
    [:div {:data-bind "with: to"}
     [:div {:data-model "user"}
      (if-let [user (if *dynamic*
                      (User.)
                      (model.subscription/get-target subscription))]
        (link-to user)
        "unknown")]]]
   [:td (if *dynamic* {:data-bind "text: created"} (:created subscription))]
   [:td (if *dynamic* {:data-bind "text: pending"} (:pending subscription))]
   [:td (if *dynamic* {:data-bind "text: local"} (:local subscription))]
   [:td (actions-section subscription)]])

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
     [:th "Actions"]]]
   [:tbody (if *dynamic* {:data-bind "foreach: $data"})
    (map #(admin-index-line % options) items)]])

(defsection admin-index-block [Subscription :viewmodel]
  [items & [page]]
  (->> items
       (map (fn [m] (index-line m page)))
       doall))

(defsection edit-button [Subscription :html]
  [item & _]
  (action-link "subscription" "edit" (:_id item)))

(defsection delete-button [Subscription :html]
  [item & _]
  (action-link "subscription" "delete" (:_id item)))






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

(defsection link-to [Subscription :html]
  [record & options]
  (let [options-map (apply hash-map options)]
    [:a (if *dynamic*
          {:data-bind "attr: {href: '/admin/subscriptions/' + ko.utils.unwrapObservable(_id)}"}
          {:href (uri record)})
     [:span (merge {:about (uri record)
                    :property "dc:title"}
                   (if *dynamic*
                     {:data-bind "text: _id"}))
      (when-not *dynamic*
        (or (:title options-map) (title record)))] ]))

;; show-section

(defsection show-section [Subscription :html]
  [item & _]
  item)

(defsection show-section [Subscription :model]
  [item & _]
  item)


(defsection subscriptions-line [Subscription :html]
  [item & [options & _]]
  [:li.subscription
   (merge {:data-id (:_id item) :data-model "subscription"})
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
  [:li.subscription {:data-id (:_item item) :data-model "subscription"}
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

