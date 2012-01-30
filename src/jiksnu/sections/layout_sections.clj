(ns jiksnu.sections.layout-sections
  (:use (ciste [config :only [environment]])
        (ciste.sections [default :only [link-to]])
        (jiksnu [session :only [current-user is-admin?]]))
  (:require (jiksnu.sections [group-sections :as sections.group]
                             [subscription-sections :as sections.subscription]
                             [user-sections :as sections.user])))

(defn navigation-section
  [response]
  (let [authenticated (current-user)
        links (concat
               [["/"                         "Public"]
                ["/users"                    "Users"]
                #_["/main/domains"             "Domains"]
                ["/groups"                   "Groups"]]
               (when authenticated
                 [["/settings/profile"         "Profile"]]))]
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


(defn left-column-section
  [authenticated subscribers subscriptions groups]
  [:aside#left-column.sidebar
   (user-info-section (current-user))
   (side-navigation)
   (sections.subscription/subscriptions-section (current-user) [])
   (sections.subscription/subscribers-section (current-user) [])
   (sections.group/user-groups (current-user))])

(defn devel-warning
  [response]
  (let [development (= :development (environment))]
    (when development
      [:div.important.devel-section.alert-message.warning
       "This site is running in development mode.
 No guarantees are made about the accuracy or security of information on this site.
 Use at your own risk."])))
