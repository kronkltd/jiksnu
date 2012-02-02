(ns jiksnu.sections.layout-sections
  (:use (ciste [config :only [environment]])
        (ciste.sections [default :only [link-to]])
        (jiksnu [session :only [current-user is-admin?]]))
  (:require (jiksnu.actions [subscription-actions :as actions.subscription])
            (jiksnu.model [subscription :as model.subscription])
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
                #_["/tags"                     "Tags"]]
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
     [:div (link-to user)]]))

(defn side-navigation
  []
  [:ul.nav.nav-list
   (when (is-admin?)
      (let [links 
            [
             #_["/admin"                    "Admin"]
             ["/admin/activities"         "Activities"]
             ["/admin/settings"           "Settings"]
             ["/admin/pshb-subscriptions" "PubSub"]
             ["/admin/users"              "Users"]
             ["/admin/subscriptions"      "Subscriptions"]]]
        (list [:li.nav-header "Admin"]
              (map
               (fn [[link title]]
                 [:li
                  [:a {:href link} title]])
               links))))])


(defn top-users
  []
  [:div
   [:p "Users with most posts"]
   [:ul
    [:li [:a {:href "#"} "#"]]]])

(defn formats-section
  [response]
  (when (:formats response)
    [:div
     [:h3 "Formats"]
     [:ul.unstyled
      (map
       (fn [format]
         [:li
          [:a {:href (:href format)} (:label format)]])
       (:formats response))]]))

(defn left-column-section
  [response authenticated subscribers subscriptions groups]
  (let [user (current-user)]
    [:aside#left-column.sidebar
     (user-info-section (or (:user response) user))
     (side-navigation)
     #_(top-users)
     (:aside response) " "
     (sections.subscription/subscriptions-section (or (:user response) user))
     (sections.subscription/subscribers-section (or (:user response) user))
     (sections.group/user-groups user)
     [:hr]
     (formats-section response)]))

(defn devel-warning
  [response]
  (let [development (= :development (environment))]
    (when development
      [:div.devel-section.alert-block.alert
       [:a.close "&times;"]
       [:h4.alert-heading "Development Mode"]
       [:p "This application is running in the " [:code ":development"] " environment. Data contained on this site may be deleted at any time and without notice. Any data provided to this application, (including user and login information) should be considered potentially at risk. " [:strong "Do not transmit sensitive information. Authentication mechanisms may become compromised."]]
       [:p "Veryify your configuration settings and restart this application with the environment variable set to "
        [:code ":production"] " to continue."]])))
