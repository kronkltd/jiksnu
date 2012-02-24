(ns jiksnu.sections.layout-sections
  (:use (ciste [core :only [apply-template]]
               [config :only [config environment]])
        (ciste.sections [default :only [add-form link-to show-section]])
        (jiksnu [session :only [current-user is-admin?]]
                [views :only [include-script]]))
  (:require (hiccup [core :as h]
                    [page-helpers :as p]
                    )
            (jiksnu [namespace :as ns])
            (jiksnu.actions [subscription-actions :as actions.subscription])
            (jiksnu.model [subscription :as model.subscription])
            (jiksnu.sections [activity-sections :as sections.activity]
                             [auth-sections :as sections.auth]
                             [group-sections :as sections.group]
                             [subscription-sections :as sections.subscription]
                             [user-sections :as sections.user]))
  (:import jiksnu.model.Activity))

(defn user-info-section
  [user]
  (when user
    (list
     (show-section user)
     (sections.subscription/subscriptions-section user)
     (sections.subscription/subscribers-section user)
     (sections.group/user-groups user))))

(defn side-navigation
  []
  (let [nav-info
        [["Home"
          [["/"                         "Public"]
           ["/users"                    "Users"]
           ["/groups"                   "Groups"]]]
         
         (when (is-admin?)
           ["Admin"
            [["/admin/activities"         "Activities"]
             ["/main/domains"             "Domains"]
             ["/admin/groups"             "Groups"]
             ["/admin/settings"           "Settings"]
             ["/admin/feed-sources"       "Feed Sources"]
             ["/admin/feed-subscriptions" "Feed Subscriptions"]
             ["/admin/keys"               "Keys"]
             ["/admin/likes"              "Likes"]
             ["/admin/users"              "Users"]
             ["/admin/subscriptions"      "Subscriptions"]]])]]
    [:ul.nav.nav-list.well
     (reduce concat
             (map
              (fn [[header links]]
                (concat [[:li.nav-header header]]
                        (map
                         (fn [[url label]]
                           [:li
                            [:a {:href url} label]])
                         links)))
              nav-info))]))

;; TODO: this will be dynamically included
(defn top-users
  []
  [:div
   [:p "Users with most posts"]
   [:ul
    [:li [:a {:href "#"} "#"]]]])

(defn formats-section
  [response]
  (when (:formats response)
    [:div.well
     [:h3 "Formats"]
     [:ul.unstyled
      (map
       (fn [format]
         [:li.format-line
          [:a {:href (:href format)}
           (when (:icon format)
             [:span.format-icon
              [:img {:src (str "/themes/classic/" (:icon format))}]])
           [:span.format-label (:label format)]]])
       (:formats response))]]))

(defn left-column-section
  [response]
  (let [user (current-user)]
    [:aside#left-column.sidebar
     (side-navigation)
     [:hr]
     (formats-section response)]))

(defn right-column-section
  [response]
  (let [user (or (:user response)
                 (current-user))]
    (list
     (user-info-section user)
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

(defn main-content
  [response]
  (list
   (when (:flash response)
     [:div#flash (:flash response)])
   (when (and (:post-form response)
              (current-user))
     (add-form (Activity.)))
   (when (:title response)
     [:h1 (:title response)])
   (:body response)))

(defn page-template-content
  [response]
  {:headers {"Content-Type" "text/html; charset=utf-8"}
   :body
   (str
    "<!doctype html>\n"
    (h/html
     [:html
      ;; TODO: Read the list of declared namespaces
      {:xmlns:sioc ns/sioc
       :xmlns:dc ns/dc
       :xmlns:foaf ns/foaf
       :xmlns:dcterms ns/dcterms
       :prefix "foaf: http://xmlns.com/foaf/0.1/
                dc: http://purl.org/dc/elements/1.1/
                sioc: http://rdfs.org/sioc/ns#
                dcterms: http://purl.org/dc/terms/"}
      [:head
       [:meta {:charset "utf-8"}]
       [:title
        (when (:title response)
          (str (:title response) " - "))
        (config :site :name)]
       (p/include-css "/bootstrap/css/bootstrap.css"
                      "/themes/classic/standard.css")
       (map
        (fn [format]
          [:link {:type (:type format)
                  :href (:href format)
                  :rel (or (:rel format) "alternate")
                  :title (:title format)}])
        (concat (:formats response)
                (:links response)))]
      [:body
       [:div.navbar.navbar-fixed-top
        [:div.navbar-inner
         [:div.container
          [:a.brand.home {:href "/"} (config :site :name)]
          ;; TODO: put a search bar here
          [:ul.nav.pull-right (sections.auth/login-section response)]]]]
       [:span#interface]
       [:div.container
        [:div.row
         [:div.span2 (left-column-section response)]
         [:div#content.span10
          [:div#notification-area.row
           [:div#flash]
           [:div.span10 (devel-warning response)]]
          [:div.row
           (if-not (:single response)
             (list [:div.span7 (main-content response)]
                   [:div.span3 (right-column-section response)])
             [:div.span10 (main-content response)])]]]
        [:footer.row.page-footer
         [:p "Copyright © 2011 KRONK Ltd."]
         [:p "Powered by " [:a {:href "https://github.com/duck1123/jiksnu"} "Jiksnu"]]]]
       [:script {:type "text/javascript"}
        "WEB_SOCKET_SWF_LOCATION = 'WebSocketMain.swf';
         var CLOSURE_NO_DEPS = true;"]
       (p/include-js "http://code.jquery.com/jquery-1.7.1.js"
                     ;; "/js/main.js"
                     "/cljs/bootstrap.js"
                     "/bootstrap/js/bootstrap.js")
       [:script {:type "text/javascript"}
          "goog.require('jiksnu.core');"]]]))})


(defmethod apply-template :html
  [request response]
  (merge response
         (if (not= (:template response) false)
           (page-template-content
            (if (:flash request)
              (assoc response :flash (:flash request))
              response)))))
