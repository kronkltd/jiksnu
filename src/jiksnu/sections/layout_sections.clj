(ns jiksnu.sections.layout-sections
  (:use (jiksnu [session :only [is-admin?]]))
  )

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
                     ["/admin/subscriptions"      "Subscriptions"]])))
               )]
    [:nav
     [:ul.nav
      (map
       (fn [[link title]]
         [:li.active
          [:a {:href link} title]])
       links)]]))
