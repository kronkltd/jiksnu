(ns jiksnu.modules.web.sections.layout-sections
  (:use [ciste.core :only [apply-template]]
        [ciste.config :only [config environment]]
        [ciste.sections.default :only [add-form show-section]]
        [clojurewerkz.route-one.core :only [named-path]]
        [jiksnu.ko :only [*dynamic*]]
        [jiksnu.modules.web.sections :only [bind-to display-property dump-data pagination-links with-sub-page]]
        [jiksnu.session :only [current-user is-admin?]])
  (:require [clojure.string :as string]
            [clojure.tools.logging :as log]
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
            [jiksnu.modules.web.sections.activity-sections :as sections.activity]
            [jiksnu.modules.web.sections.auth-sections :as sections.auth]
            [jiksnu.modules.web.sections.group-sections :as sections.group]
            [jiksnu.modules.web.sections.stream-sections :as sections.stream]
            [jiksnu.modules.web.sections.subscription-sections :as sections.subscription]
            [jiksnu.modules.web.sections.user-sections :as sections.user])
  (:import jiksnu.model.Activity
           jiksnu.model.User))

(def statistics-info
  [["activities"        "Activities"]
   ["conversations"     "Conversations"]
   ["domains"           "Domains"]
   ["groups"            "Groups"]
   ["feedSources"       "Feed Sources"]
   ["feedSubscriptions" "Feed Subscriptions"]
   ["subscriptions"     "Subscriptions"]
   ["users"             "Users"]])

(defn nav-info
  []
  [["Home"
    [[(named-path "public timeline")     "Public"]
     [(named-path "index users")         "Users"]
     ;; [(named-path "index conversations") "Conversations"]
     [(named-path "index feed-sources")  "Feeds"]
     [(named-path "index domains")       "Domains"]
     [(named-path "index groups")        "Groups"]
     [(named-path "index resources")     "Resources"]]]
   ["Settings"
    [["/admin/settings"           "Settings"]]]
   (when (is-admin?)
     ["Admin"
      [
       ["/admin/activities"         "Activities"]
       ["/admin/auth"               "Auth"]
       ["/admin/clients"            "Clients"]
       ["/admin/conversations"      "Conversations"]
       ["/admin/feed-sources"       "Feed Sources"]
       ["/admin/feed-subscriptions" "Feed Subscriptions"]
       ["/admin/groups"             "Groups"]
       ["/admin/keys"               "Keys"]
       ["/admin/likes"              "Likes"]
       ["/admin/request-tokens"     "Request Tokens"]
       ["/admin/streams"            "Streams"]
       ["/admin/subscriptions"      "Subscriptions"]
       ["/admin/users"              "Users"]
       ["/admin/workers"            "Workers"]
       ]])])

(defn user-info-section
  [user]
  (list
   (show-section user)
   [:div {:data-model "user"}
    (sections.subscription/subscriptions-widget user)
    (sections.subscription/subscribers-widget user)
    (sections.group/user-groups user)
    (with-sub-page "streams"
     (sections.stream/streams-widget user))]))

(defn navigation-group
  [[header links]]
  (concat [[:li.nav-header header]]
          (map
           (fn [[url label]]
             [:li [:a {:href url} label]])
           links)))

(defn side-navigation
  []
  [:ul.nav.nav-list.well
   (->> (nav-info)
        (map navigation-group)
        (reduce concat))])

;; TODO: this will be dynamically included
(defn top-users
  []
  [:div
   [:p "Users with most posts"]
   [:ul
    [:li [:a {:href "#"} "#"]]]])

(defn formats-section*
  [format]
  [:li.format-line
   [:a
    (if *dynamic*
      {:data-bind "attr: {href: href}"}
      {:href (:href format)})
    [:span.format-icon
     [:img (merge {:alt ""}
                  (if *dynamic*
                    {:data-bind "attr: {src: '/assets/themes/classic/' + icon}"}
                    {:src (str "/assets/themes/classic/" (:icon format))}))]]
    [:span.format-label
     (display-property format :label)]]])

(defn formats-section
  [response]
  (when-let [formats (if *dynamic*
                       [{}]
                       (:formats response))]
    [:div.well
     [:h3 "Formats"]
     [:ul.unstyled
      (when *dynamic*
        {:data-bind "foreach: formats"})
      (map formats-section* formats)]]))

(defn statistics-line
  [stats [model-name label]]
  [:tr
   [:td.stat-label label]
   [:td.stat-value
    (if *dynamic*
      {:data-bind (format "text: %s" model-name)}
      (get stats model-name))]])

(defn statistics-section
  [request response]
  (let [stats (actions.site/get-stats)]
    [:div.well.statistics-section
     (bind-to "statistics"
       [:table.table.table-compact
        [:thead
         [:tr
          [:th "Collection"]
          [:th "Count"]]]
        [:tbody
         (map (partial statistics-line stats) statistics-info)]])]))

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

(defn notification-line
  [message]
  [:li.alert
   [:button (merge {:class "close"}
                   (when *dynamic*
                     {:data-bind "click: $parent.dismissNotification"}))
    "x"]
   [:span (if *dynamic*
            {:data-bind "text: message"}
            message)]])

(defn notification-area
  [request response]
  [:div#flash
   [:ul.unstyled
    (when *dynamic* {:data-bind "foreach: notifications"})
    (if *dynamic*
      (notification-line nil)
      (when-let [flash (:flash request)] (notification-line flash)))]])

(defn new-post-section
  [request response]
  (bind-to "postForm"
    [:div (when *dynamic* {:data-bind "if: visible"})
     (when (or *dynamic* (:post-form response))
       (add-form (Activity.)))]))

(defn title-section
  [request response]
  [:h1 (if *dynamic*
         {:data-bind "text: title"}
         (:title response))])

(defn fork-me-link
  []
  [:a {:href "http://github.com/duck1123/jiksnu"}
   [:img {:style "position: absolute; top: 43px; right: 0; border: 0;"
          :src "https://a248.e.akamai.net/assets.github.com/img/7afbc8b248c68eb468279e8c17986ad46549fb71/687474703a2f2f73332e616d617a6f6e6177732e636f6d2f6769746875622f726962626f6e732f666f726b6d655f72696768745f6461726b626c75655f3132313632312e706e67"
          :alt "Fork me on GitHub"}]])

(defn navbar-search-form
  []
  [:form.navbar-search.pull-left
   {:action "/main/search" :method "post"}
   [:input.search-query.span3
    {:type "text" :placeholder "Search" :name "q"}]])

(defn style-section
  []
  (let [theme (config :site :theme)]
    (p/include-css
     (if (= theme "classic")
       "/assets/js/bootstrap/2.3.2/css/bootstrap.min.css"
       (format "http://bootswatch.com/%s/bootstrap.min.css" theme))
     "/assets/themes/classic/standard.css"
     "/assets/js/bootstrap/2.3.2/css/bootstrap-responsive.min.css")))

(defn get-prefixes
  []
  (->> [["foaf" ns/foaf]
        ["dc" ns/dc]
        ["sioc" ns/sioc]
        ["dcterms" "http://purl.org/dc/terms/"]]
       (map
        (fn [[prefix uri]] (format "%s: %s" prefix uri)))
       (string/join " ")))

(defn navbar-section
  [request response]
  [:div.navbar.navbar-fixed-top
   [:div.navbar-inner
    [:div.container-fluid
     [:a.btn.btn-navbar {:data-toggle "collapse"
                         :data-target ".nav-collapse"}
      [:span.icon-bar]
      [:span.icon-bar]
      [:span.icon-bar]]
     [:a.brand.home {:href "/" :rel "top"}
      (config :site :name)]
     [:div.nav-collapse.collapse
      ;; (navbar-search-form)
      [:ul.nav.pull-right (sections.auth/login-section response)]
      #_[:div.navbar-text.connection-info.pull-right]
      #_[:div.navbar-text.pull-right
       (if *dynamic* "dynamic" "static")]
      [:div.visible-tablet.visible-phone
       (side-navigation)]]]]])

(defn links-section
  [request response]
  (map
   (fn [format]
     [:link format])
   (concat (:formats response)
           (:links response)
           [{:href "/rsd.xml"
             :type "application/rsd+xml"
             :rel "EditURI"}
            #_{:href "/opensearch/notices"
             :title "Notice Search"
             :type "application/opensearchdescription+xml"
             :rel "search"}
            #_{:href "/opensearch/people"
             :title "People Search"
             :type "application/opensearchdescription+xml"
             :rel "search"}
            {:href (str "http://" (config :domain) "/favicon.ico")
             :rel "shortcut icon"}])))

(defonce scripts-section-hook (ref []))

(defn scripts-section
  [request response]
  (let [websocket-path (str "ws://" (config :domain) ":" (config :http :port) "/websocket")]
    (list
    [:script {:type "text/javascript"}
     ;; "WEB_SOCKET_SWF_LOCATION = 'WebSocketMain.swf';"
     (format "WEBSOCKET_PATH = '%s';" websocket-path)
     "var CLOSURE_NO_DEPS = true;"]
    (p/include-js
     ;; "/assets/js/modernizr-2.6.1.js"
     "/assets/js/underscore/1.4.4/underscore.min.js"
     "/assets/js/jquery/1.10.1/jquery.js"
     "/assets/js/jquery.timeago/1.3.0/jquery.timeago.js"
     "/assets/js/knockout/2.2.1/knockout.js"
     "/assets/js/bootstrap/2.3.2/js/bootstrap.min.js"
     "/assets/js/bootstrap-markdown/1.0.0/js/bootstrap-markdown.js"
     "/assets/js/backbone/1.0.0/backbone.min.js"
     "/assets/js/knockback/0.17.2/knockback.js"
     "/assets/js/jiksnu.js")
    (doall
     (map (fn [hook]
            (hook request response))
          @scripts-section-hook))
    [:script {:type "text/javascript"}
     "goog.require('jiksnu.core');"])))

(defn right-column-section
  [response]
  (let [user (if *dynamic*
               (User.)
               (or (:user response)
                  (current-user)))]
    (list
     (bind-to "$root.targetUser() || $root.currentUser()"
       (user-info-section user))
     (:aside response))))

(defn main-content
  [request response]
  [:section#main
   (notification-area request response)
   (when (current-user)
     (new-post-section request response))
   (title-section request response)
   (:body response)
   [:footer.row-fluid.page-footer
    [:p "Copyright © 2011 KRONK Ltd."]
    [:p "Powered by " [:a {:href "https://github.com/duck1123/jiksnu"} "Jiksnu"]]]])

(defn page-template-content
  [request response]
  {:headers {"Content-Type" "text/html; charset=utf-8"}
   :body
   (str
    "<!DOCTYPE html" ">"
    (h/html
     [:html
      ;; TODO: Read the list of declared namespaces
      (merge {:xmlns:sioc ns/sioc
              :xmlns:dc ns/dc
              :xmlns:foaf ns/foaf
              :xmlns:dcterms ns/dcterms
              ;; :version "HTML+RDFa 1.1"
              :lang "en"
              :xml:lang "en"
              :prefix (get-prefixes) })
      [:head
       [:meta {:charset "UTF-8"}]
       [:title {:property "dc:title"}
        (when-not *dynamic*
          (str (when (:title response)
                 (str (:title response) " - "))
               (config :site :name)))]
       [:meta {:name "viewport"
               :content "width=device-width, initial-scale=1.0"}]
       (style-section)
       (links-section request response)]
      [:body {:data-dynamic (str *dynamic*)}
       (navbar-section request response)
       [:div.container-fluid
        (when *dynamic*
          {:data-bind "if: loaded"})
        [:div.row-fluid
         [:div.span2.hidden-tablet.hidden-phone
          [:aside#left-column.sidebar.hidden-tablet
           (side-navigation)
           [:hr]
           (formats-section response)
           #_(statistics-section request response)]]
         [:div#content.span10
          [:div.row-fluid
           (if-not (:single response)
             (list [:div.span10 (main-content request response)]
                   [:div.span2 (right-column-section response)])
             [:div.span12 (main-content request response)])]]]]
       (scripts-section request response)]]))})


(defmethod apply-template :html
  [request response]
  (merge response
         (if (not= (:template response) false)
           (page-template-content request
                                  (if (:flash request)
                                    (assoc response :flash (:flash request))
                                    response)))))
