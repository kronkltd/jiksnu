(ns jiksnu.sections.layout-sections
  (:use (ciste [config :only [environment]])
        (ciste.sections [default :only [link-to]])
        (jiksnu [session :only [current-user is-admin?]]))
  (:require (jiksnu.actions [subscription-actions :as actions.subscription])
            (jiksnu.model [subscription :as model.subscription])
            (jiksnu.sections [group-sections :as sections.group]
                             [subscription-sections :as sections.subscription]
                             [user-sections :as sections.user])))

(defn user-info-section
  [user]
  (when user
    [:div#user-info
     (sections.user/display-avatar user)
     [:div (link-to user)]]))

(defn side-navigation
  []
  [:ul.nav.nav-list.well
   (let [nav-info
         [["Home"
           [["/"                         "Public"]
            ["/users"                    "Users"]
            ["/groups"                   "Groups"]]]
          
          (when (is-admin?)
            ["Admin"
             [["/admin/activities"         "Activities"]
              ["/admin/settings"           "Settings"]
              ["/admin/pshb-subscriptions" "PubSub"]
              ["/admin/users"              "Users"]
              ["/admin/subscriptions"      "Subscriptions"]]])]]
     (reduce concat
             (map
              (fn [[header links]]
                (concat [[:li.nav-header header]]
                        (map
                         (fn [[url label]]
                           [:li
                            [:a {:href url} label]])
                         links)))
              nav-info)))])


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
          [:a {:href (:href format)}
           (when (:icon format)
             [:img {:src (str "/themes/classic/" (:icon format))}])
           (:label format)]])
       (:formats response))]]))

(defn left-column-section
  [response authenticated subscribers subscriptions groups]
  (let [user (current-user)]
    [:aside#left-column.sidebar
     (side-navigation)
     #_(top-users)
     [:hr]
     (formats-section response)]))

(defn right-column-section
  [response]
  (let [user (current-user)]
    (list
     (user-info-section (or (:user response) user))
     (sections.subscription/subscriptions-section (or (:user response) user))
     (sections.subscription/subscribers-section (or (:user response) user))
     (sections.group/user-groups user)
     (:aside response))))

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
