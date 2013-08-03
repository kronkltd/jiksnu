(ns jiksnu.modules.core.sections.subscription-sections
  (:use [ciste.sections :only [declare-section defsection]]
        [ciste.sections.default :only [delete-button edit-button full-uri
                                       index-block index-line index-section link-to
                                       show-section title uri]]
        [clojurewerkz.route-one.core :only [named-path]]
        [jiksnu.ko :only [*dynamic*]]
        [jiksnu.modules.web.sections :only [action-link admin-index-block admin-index-line
                                admin-index-section bind-to control-line dump-data with-page
                                with-sub-page]])
  (:require [ciste.model :as cm]
            [clj-tigase.core :as tigase]
            [clojure.tools.logging :as log]
            [jiksnu.model.subscription :as model.subscription]
            [jiksnu.model.user :as model.user]
            [jiksnu.namespace :as ns]
            [jiksnu.modules.web.sections.user-sections :as sections.user])
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

(defn subscriber-response-element
  [subscription]
  (let [subscriber (model.user/fetch-by-id (:from subscription))]
    ["subscriber" {"node" ns/microblog
                   "created" (:created subscription)
                   "jid" (str (:username subscriber) "@"
                              (:domain subscriber))}]))

(defn subscription-response-element
  [subscription]
  (let [subscribee (model.user/fetch-by-id (:to subscription))]
    ["subscription" {"node" ns/microblog
                     "subscription" "subscribed"
                     "created" (:created subscription)
                     "jid" (str (:username subscribee) "@"
                                (:domain subscribee))}]))

(defn unsubscription-request
  [subscription]
  (let [subscribee (model.user/fetch-by-id (:from subscription))]
    ["pubsub"  {"xmlns" ns/pubsub}
     ["unsubscribe" {"node" ns/microblog
                     "jid" (tigase/make-jid subscribee)}]]))

(defn subscribe-request
  [subscription]
  (let [subscribee (model.user/fetch-by-id (:from subscription))]
    ["pubsub"  {"xmlns" ns/pubsub}
     ["subscribe" {"node" ns/microblog
                   "jid" (tigase/make-jid subscribee)}]]))

(defn subscribers-response
  [subscribers]
  ["pubsub" {"xmlns" ns/pubsub}
   ["subscribers" {"node" ns/microblog}
    (map subscriber-response-element subscribers)]])

(defn subscriptions-response
  "Returns a response iq packet containing the ids in entries"
  [subscriptions]
  ["pubsub" {"xmlns" ns/pubsub}
   ["subscriptions" {"node" ns/microblog}
    (map subscription-response-element subscriptions)]])

(defn actions-section
  [subscription]
  [:div.btn-group
   [:a.btn.dropdown-toggle {:data-toggle "dropdown"}
    [:span.caret]]
   [:ul.dropdown-menu.pull-right
    [:li (delete-button subscription)]]])


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
              {:data-bind "attr: {href: '/users/' + _id() + '/subscribers'}"}
              {:href (named-path "user subscribers" {:id (:_id user)})}) "Followers"]
        " "
        (with-sub-page "subscribers"
          [:span (if *dynamic*
                   {:data-bind "text: items().length"}
                   (count subscriptions))])]
       (with-sub-page "subscribers"
         [:ul.unstyled
          (if *dynamic* {:data-bind "foreach: items"})
          (map (fn [subscription]
                 [:li {:data-model "subscription"}
                  (bind-to "from"
                    [:div {:data-model "user"}
                     (let [user (if *dynamic*
                                  (User.)
                                  (model.subscription/get-actor subscription))]
                       (sections.user/display-avatar user "24"))])])
               subscriptions)])])))

(defn subscriptions-widget
  [user]
  (when user
    (let [subscriptions (if *dynamic*
                          [(Subscription.)]
                          (model.subscription/subscriptions user))]
      [:div.subscriptions
       [:h3
        [:a (if *dynamic*
              {:data-bind "attr: {href: '/users/' + _id() + '/subscriptions'}"}
              {:href (str (full-uri user) "/subscriptions")}) "Following"]
        " "
        (with-sub-page "subscriptions"
          [:span (if *dynamic*
                   {:data-bind "text: items().length"}
                   (count subscriptions))])]
       (with-sub-page "subscriptions"
         [:ul (when *dynamic* {:data-bind "foreach: items"})
          (map (fn [subscription]
                 [:li {:data-model "subscription"}
                  (bind-to "to"
                    [:div {:data-model "user"}
                     (let [user (if *dynamic*
                                  (User.)
                                  (model.subscription/get-target subscription))]
                       (sections.user/display-avatar user "24"))])]) subscriptions)])
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
    (bind-to "from"
      [:div {:data-model "user"}
       (if-let [user (if *dynamic*
                       (User.)
                       (model.subscription/get-actor subscription))]
         (link-to user)
         "unknown")])]
   [:td
    (bind-to "to"
      [:div {:data-model "user"}
       (if-let [user (if *dynamic*
                       (User.)
                       (model.subscription/get-target subscription))]
         (link-to user)
         "unknown")])]
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
   [:tbody (when *dynamic* {:data-bind "foreach: items"})
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
  [:li.subscription {:data-model "subscription"}
   (bind-to "to"
     (if-let [user (if *dynamic* (User.) (model.subscription/get-target item))]
       (show-section user)
       "unknown"))])

(defsection subscriptions-block [Subscription :html]
  [items & [options & _]]
  [:ul.subscriptions {:data-bind "foreach: items"}
   (map (fn [item] (subscriptions-line item options)) items)])

(defsection subscriptions-section [Subscription :html]
  [items & [options & _]]
  (subscriptions-block items options))

(defsection subscribers-line [Subscription :html]
  [item & [options & _]]
  [:li.subscription {:data-model "subscription"}
   (bind-to "from"
     (if-let [user (if *dynamic* (User.) (model.subscription/get-actor item))]
       (show-section user)
       "unknown"))])

(defsection subscribers-block [Subscription :html]
  [items & [options & _]]
  [:ul.subscriptions {:data-bind "foreach: items"}
   (map (fn [item] (subscribers-line item options)) items)])

(defsection subscribers-section [Subscription :html]
  [items & [options & _]]
  (subscribers-block items options))


(defsection uri [Subscription]
  [subscription & _]
  (str "/admin/subscriptions/" (:_id subscription)))

