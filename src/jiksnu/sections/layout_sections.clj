(ns jiksnu.sections.layout-sections
  (:use (ciste.sections [default :only [link-to]])
        (jiksnu [session :only [is-admin?]]))
  (:require (jiksnu.sections [group-sections :as sections.group]
                             [subscription-sections :as sections.subscription]
                             [user-sections :as sections.user])))

(defn navigation-section
  [authenticated]
  (let [links (concat
               [["/"                         "Public"]
                ["/users"                    "Users"]
                ["/main/domains"             "Domains"]
                ["/groups"                   "Groups"]]
               (when authenticated
                 (concat
                  [["/settings/profile"         "Profile"]]
                  (when (is-admin?)
                    [["/admin"                    "Admin"]
                     ["/admin/activities"         "Activities"]
                     ["/admin/settings"           "Settings"]
                     ["/admin/pshb-subscriptions" "PubSub"]
                     ["/admin/users"              "Users"]
                     ["/admin/subscriptions"      "Subscriptions"]]))))]
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


(defn left-column-section
  [authenticated subscribers subscriptions groups]
  [:aside#left-column.sidebar
   (user-info-section)
   (sections.subscription/subscriptions-section)
   (sections.subscription/subscribers-section)
   (sections.group/user-groups)])

(defn devel-warning
  [development]
  (when development
    [:div.important.devel-section.alert-message.warning
     "This site is running in development mode.
 No guarantees are made about the accuracy or security of information on this site.
 Use at your own risk."]))
