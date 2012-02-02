(ns jiksnu.sections.layout-sections
  (:use (ciste [config :only [environment]])
        (ciste.sections [default :only [link-to]])
        (jiksnu [session :only [current-user is-admin?]]))
  (:require (jiksnu.actions [subscription-actions :as actions.subscription])
            (jiksnu.sections [group-sections :as sections.group]
                             [subscription-sections :as sections.subscription]
                             [user-sections :as sections.user])))

(defn navigation-section
  [response]
  (let [authenticated (current-user)
        links (concat
               [["/"                         "Public"]
                ["/users"                    "Users"]
                #_["/main/domains"             "Domains"]
                ["/groups"                   "Groups"]
                ["/tags"                     "Tags"]]
               (when authenticated
                 [["/settings/profile"         "Profile"]
                  [(str "/" (:username authenticated) "/inbox") "Inbox"]
                  [(str "/" (:username authenticated) "/outbox") "Outbox"]
                  ]))]
    [:nav
     [:ul.nav
      (map
       (fn [[link title]]
         [:li.active
          [:a {:href link} title]])
       links)]]))


(defn user-info-section
  [user]
  (when user
    [:div#user-info
     (sections.user/display-avatar user)
     (link-to user)]))

(defn side-navigation
  []
  (let [links (when (is-admin?)
                [["/admin"                    "Admin"]
                 ["/admin/activities"         "Activities"]
                 ["/admin/settings"           "Settings"]
                 ["/admin/pshb-subscriptions" "PubSub"]
                 ["/admin/users"              "Users"]
                 ["/admin/subscriptions"      "Subscriptions"]])]
    [:nav
     [:ul.unstyled
      (map
       (fn [[link title]]
         [:li.active
          [:a {:href link} title]])
       links)]]))

(defn top-users
  []
  [:div
   [:p "Users with most posts"]
   [:ul
    [:li [:a {:href "#"} "#"]]]])

(defn formats-section
  [response]
  [:div
   [:h3 "Formats"]
   [:ul
    (map
     (fn [format]
       [:li
        [:a {:href (:href format)} (:label format)]])
     (:formats response))]])

(defn left-column-section
  [response authenticated subscribers subscriptions groups]
  [:aside#left-column.sidebar
   (user-info-section (current-user))
   (side-navigation)
   (top-users)
   (:aside response)
   (sections.subscription/subscriptions-section (current-user) [])
   (sections.subscription/subscribers-section (current-user) [])
   (sections.group/user-groups (current-user))
   (formats-section response)])

(defn devel-warning
  [response]
  (let [development (= :development (environment))]
    (when development
      [:div.important.devel-section.alert-message.warning
       "This site is running in development mode.
 No guarantees are made about the accuracy or security of information on this site.
 Use at your own risk."])))
