(ns jiksnu.sections.layout-sections
  (:use [ciste.core :only [apply-template]]
        [ciste.config :only [config environment]]
        [ciste.sections.default :only [add-form link-to show-section]]
        [jiksnu.session :only [current-user is-admin?]])
  (:require [clojure.tools.logging :as log]
            [hiccup.core :as h]
            [hiccup.page :as p]
            [jiksnu.namespace :as ns]
            [jiksnu.actions.site-actions :as actions.site]
            [jiksnu.actions.subscription-actions :as actions.subscription]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.conversation :as model.conversation]
            [jiksnu.model.domain :as model.domain]
            [jiksnu.model.group :as model.group]
            [jiksnu.model.feed-source :as model.feed-source]
            [jiksnu.model.feed-subscription :as model.feed-subscription]
            [jiksnu.model.subscription :as model.subscription]
            [jiksnu.model.user :as model.user]
            [jiksnu.sections.activity-sections :as sections.activity]
            [jiksnu.sections.auth-sections :as sections.auth]
            [jiksnu.sections.group-sections :as sections.group]
            [jiksnu.sections.subscription-sections :as sections.subscription]
            [jiksnu.sections.user-sections :as sections.user])
  (:import jiksnu.model.Activity))

(defn user-info-section
  [user]
  (when user
    (list
     (show-section user)
     (sections.subscription/subscriptions-widget user)
     (sections.subscription/subscribers-widget user)
     (sections.group/user-groups user))))

(defn side-navigation
  []
  (let [nav-info
        [["Home"
          [["/"                         "Public"]
           ["/users"                    "Users"]
           ["/main/domains"             "Domains"]
           ["/groups"                   "Groups"]]]
         
         (when (is-admin?)
           ["Admin"
            [["/admin/activities"         "Activities"]
             ["/admin/auth"               "Auth"]
             ["/admin/conversations"      "Conversations"]
             ["/admin/groups"             "Groups"]
             ["/admin/settings"           "Settings"]
             ["/admin/feed-sources"       "Feed Sources"]
             ["/admin/feed-subscriptions" "Feed Subscriptions"]
             ["/admin/keys"               "Keys"]
             ["/admin/likes"              "Likes"]
             ["/admin/users"              "Users"]
             ["/admin/subscriptions"      "Subscriptions"]
             ["/admin/workers"            "Workers"]]])]]
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
              [:img {:alt ""
                     :src (str "/themes/classic/" (:icon format))}]])
           [:span.format-label (:label format)]]])
       (:formats response))]]))

(defn statistics-section
  [response]
  (let [stats (actions.site/get-stats)]
    [:div.well.statistics-section
     [:table.table.table-compact
      [:thead
       [:tr
        [:th "Collection"]
        [:th "Count"]]]
      [:tbody
       [:tr {:data-model "activities"}
       [:td.stat-label "Activities: "]
        [:td.stat-value (:activities stats)]]
       [:tr {:data-model "conversations"}
        [:td.stat-label "Conversations: "]
        [:td.stat-value (:conversations stats)]]
       [:tr {:data-model "domains"}
        [:td.stat-label "Domains: "]
        [:td.stat-value (:domains stats)]]
       [:tr {:data-model "groups"}
        [:td.stat-label "Groups: "]
        [:td.stat-value (:groups stats)]]
       [:tr {:data-model "feed-sourcces"}
        [:td.stat-label "Feed Sources: "]
        [:td.stat-value (:feed-sources stats)]]
       [:tr {:data-model "feed-subscriptions"}
        [:td.stat-label "Feed Subscriptions: "]
        [:td.stat-value (:feed-subscriptions stats)]]
       [:tr {:data-model "subscriptions"}
        [:td.stat-label "Subscriptions: "]
        [:td.stat-value (:subscriptions stats)]]
       [:tr {:data-model "users"}
        [:td.stat-label "Users: "]
        [:td.stat-value (:users stats)]]]]]))

(defn left-column-section
  [response]
  (let [user (current-user)]
    [:aside#left-column.sidebar
     (side-navigation)
     [:hr]
     (formats-section response)
     (statistics-section response)]))

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
  [request response]
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
  [request response]
  {:headers {"Content-Type" "text/html; charset=utf-8"}
   :body
   (str
    "<!DOCTYPE html"

    ;; " PUBLIC \"-//W3C//DTD XHTML+RDFa 1.0//EN\"
    ;;       \"http://www.w3.org/MarkUp/DTD/xhtml-rdfa-1.dtd\""
    ">"
    (h/html
     [:html
      ;; TODO: Read the list of declared namespaces
      {
       :xmlns:sioc ns/sioc
       :xmlns:dc ns/dc
       :xmlns:foaf ns/foaf
       :xmlns:dcterms ns/dcterms
       ;; :version "HTML+RDFa 1.1"
       :lang "en"
       :xml:lang "en"
       :prefix "foaf: http://xmlns.com/foaf/0.1/ dc: http://purl.org/dc/elements/1.1/ sioc: http://rdfs.org/sioc/ns# dcterms: http://purl.org/dc/terms/"
       }
      [:head
       [:meta {:charset "UTF-8"}]
       [:title {:property "dc:title"}
        (when (:title response)
          (str (:title response) " - "))
        (config :site :name)]
       (p/include-css "/assets/bootstrap-2.4.0/css/bootstrap.min.css"
                      "/assets/bootstrap-2.4.0/css/bootstrap-responsive.min.css"
                      "/assets/themes/classic/standard.css")
       [:link {:href (str "http://" (config :domain) "/favicon.ico")
               :rel "shortcut icon"}]
       #_[:link {:href "/opensearch/people"
               :title "People Search"
               :type "application/opensearchdescription+xml"
               :rel "search"}]
       #_[:link {:href "/opensearch/notices"
               :title "Notice Search"
               :type "application/opensearchdescription+xml"
               :rel "search"}]
       [:link {:href "/rsd.xml"
               :type "application/rsd+xml"
               :rel "EditURI"}]
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
         [:div.container-fluid
          [:a.brand.home {:href "/" :rel "top"} (config :site :name)]
          ;; [:form.navbar-search.pull-left
          ;;  {:action "/main/search" :method "post"}
          ;;  [:input.search-query.span3
          ;;   {:type "text" :placeholder "Search" :name "q"}]]
          [:div.navbar-text.connection-info.pull-right ]
          [:ul.nav.pull-right (sections.auth/login-section response)]]]]
       [:div.container-fluid
        #_[:a {:href "http://github.com/duck1123/jiksnu"}
         [:img {:style "position: absolute; top: 43px; right: 0; border: 0;"
                :src "https://a248.e.akamai.net/assets.github.com/img/7afbc8b248c68eb468279e8c17986ad46549fb71/687474703a2f2f73332e616d617a6f6e6177732e636f6d2f6769746875622f726962626f6e732f666f726b6d655f72696768745f6461726b626c75655f3132313632312e706e67"
                :alt "Fork me on GitHub"}]]
        [:div.row-fluid
         [:div.span2
          #_[:span#interface]
          (left-column-section response)]
         [:div#content.span10
          [:div#notification-area.row-fluid
           [:div#flash (:flash request)]
           #_[:div.span10 (devel-warning response)]]
          [:div.row-fluid
           (if-not (:single response)
             (list [:div.span9 (main-content request response)]
                   [:div.span3 (right-column-section response)])
             [:div.span12 (main-content request response)])]]]
        [:footer.row-fluid.page-footer
         [:p "Copyright © 2011 KRONK Ltd."]
         [:p "Powered by " [:a {:href "https://github.com/duck1123/jiksnu"} "Jiksnu"]]]]
       [:script {:type "text/javascript"}
        (str "WEB_SOCKET_SWF_LOCATION = 'WebSocketMain.swf';"
             "WEBSOCKET_PATH = "
             "'ws://" (config :domain) ":" (config :http :port) "/websocket'"
             ";"
             "var CLOSURE_NO_DEPS = true;")]
       (p/include-js
        "/assets/web-socket-js/swfobject.js"
        "/assets/web-socket-js/web_socket.js"
        "http://code.jquery.com/jquery-1.7.1.js"
        "https://ajax.googleapis.com/ajax/libs/jqueryui/1.8.21/jquery-ui.min.js"
        "/cljs/bootstrap.js"
        "/assets/bootstrap-2.4.0/js/bootstrap.min.js"
)
       [:script {:type "text/javascript"}
        "goog.require('jiksnu.core');"]]]))})


(defmethod apply-template :html
  [request response]
  (merge response
         (if (not= (:template response) false)
           (page-template-content request
            (if (:flash request)
              (assoc response :flash (:flash request))
              response)))))
