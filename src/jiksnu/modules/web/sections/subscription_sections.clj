(ns jiksnu.modules.web.sections.subscription-sections
  (:require [ciste.model :as cm]
            [ciste.sections :refer [declare-section defsection]]
            [ciste.sections.default :refer [actions-section delete-button edit-button full-uri
                                            index-block index-line index-section link-to
                                            show-section title uri]]
            [clojure.tools.logging :as log]
            [jiksnu.ko :refer [*dynamic*]]
            [jiksnu.model.subscription :as model.subscription]
            [jiksnu.model.user :as model.user]
            [jiksnu.namespace :as ns]
            [jiksnu.modules.core.sections :refer [admin-index-block admin-index-line
                                                  admin-index-section]]
            [jiksnu.modules.core.sections.subscription-sections :refer [subscribers-block
                                                                        subscribers-line
                                                                        subscribers-section
                                                                        subscriptions-block
                                                                        subscriptions-line
                                                                        subscriptions-section]]
            [jiksnu.modules.web.sections :refer [action-link bind-to control-line
                                                 with-page with-sub-page]]
            [jiksnu.modules.web.sections.user-sections :as sections.user])
  (:import jiksnu.model.Subscription
           jiksnu.model.User))

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
  (let [subscriptions (if *dynamic*
                        [(Subscription.)]
                        (model.subscription/subscribers user))]
    [:div.subscribers
     [:h3
      [:a (if *dynamic*
            {:data-bind "attr: {href: '/users/' + _id() + '/subscribers'}"}
            {:href (str "/users/" (:_id user) "/subscribers")}) "Followers"]
      " "
      (with-sub-page "subscribers"
        [:span "{{page.items.length}}"])]
     (with-sub-page "subscribers"
       [:ul.unstyled
        (let [subscription (first subscriptions)]
          [:li {:data-model "subscription"
                :ng-repeat "subscription in subscriptions"}
           (bind-to "from"
                    [:div {:data-model "user"}
                     (let [user (if *dynamic*
                                  (User.)
                                  (model.subscription/get-actor subscription))]
                       (sections.user/display-avatar user "24"))])])])]))

(defn subscriptions-widget
  [user]
  (when user
    (let [subscriptions (if *dynamic*
                          [(Subscription.)]
                          (model.subscription/subscriptions user))]
      [:div.subscriptions
       [:h3
        [:a {:href "/users/{{user.id}}/subscriptions"} "Following"]
        " "
        (with-sub-page "subscriptions"
          [:span "{{page.items.length}}"])]
       (with-sub-page "subscriptions"
         [:ul
          (let [subscription (first subscriptions)]
            [:li {:data-model "subscription"
                  :ng-repeat "subscription in subscriptions"}
             (bind-to "to"
                      [:div {:data-model "user"}
                       (let [user (if *dynamic*
                                    (User.)
                                    (model.subscription/get-target subscription))]
                         (sections.user/display-avatar user "24"))])])])
       [:p
        [:a {:href "/main/ostatussub"} "Add Remote"]]])))

(defsection actions-section [Subscription :html]
  [subscription]
  [:div.btn-group
   [:a.btn.dropdown-toggle {:data-toggle "dropdown"}
    [:span.caret]]
   [:ul.dropdown-menu.pull-right
    [:li (delete-button subscription)]]])

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
   [:tbody
    (let [subscription (first items)]
      [:tr {:ng-repeat "subscription in subscriptions"
            :data-model "subscription"
            :data-id "{{subscription.id}}"}
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
       [:td "{{subscription.created}}"]
       [:td "{{subscription.pending}}"]
       [:td "{{subscription.local}}"]
       [:td (actions-section subscription)]])]])

(defsection edit-button [Subscription :html]
  [item & _]
  (action-link "subscription" "edit" (:_id item)))

(defsection delete-button [Subscription :html]
  [item & _]
  (action-link "subscription" "delete" (:_id item)))

(defsection link-to [Subscription :html]
  [record & options]
  [:a {:href "/admin/subscriptions/{{subscription.id}}"}
   [:span {:about "{{subscription.uri}}"
           :property "dc:title"}
    "{{subscription.id}}"]])

;; show-section

(defsection show-section [Subscription :html]
  [item & _]
  item)

(defsection subscriptions-section [Subscription :html]
  [items & [options & _]]
  [:ul.subscriptions
   (let [item (first items)]
     [:li.subscription {:data-model "subscription"
                        :ng-repeat "subscription in subscriptions"}
      (bind-to "to"
               (if-let [user (if *dynamic* (User.)
                                 (model.subscription/get-target item))]
                 (show-section user)
                 "unknown"))])])

(defsection subscribers-section [Subscription :html]
  [items & [options & _]]
  [:ul.subscriptions
   (let [item (first items)]
     [:li.subscription {:data-model "subscription"
                        :ng-repeat "subscriber in subscribers"}
      (bind-to "from"
               (if-let [user (if *dynamic* (User.) (model.subscription/get-actor item))]
                 (show-section user)
                 "unknown"))])]

  )
