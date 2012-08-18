(ns jiksnu.sections.layout-sections
  (:use [ciste.core :only [apply-template]]
        [ciste.config :only [config environment]]
        [ciste.sections.default :only [add-form link-to show-section]]
        [jiksnu.ko :only [*dynamic*]]
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
            [jiksnu.sections.activity-sections :as sections.activity]
            [jiksnu.sections.auth-sections :as sections.auth]
            [jiksnu.sections.group-sections :as sections.group]
            [jiksnu.sections.subscription-sections :as sections.subscription]
            [jiksnu.sections.user-sections :as sections.user])
  (:import jiksnu.model.Activity))

(defn user-info-section
  [user]
  (list
   (show-section user)
   (sections.subscription/subscriptions-widget user)
   (sections.subscription/subscribers-widget user)
   (sections.group/user-groups user)))

(defn nav-info
  []
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
       ["/admin/workers"            "Workers"]]])])

(defn navigation-group
  [[header links]]
  (concat [[:li.nav-header header]]
          (map
           (fn [[url label]]
             [:li
              [:a {:href url} label]])
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
                     :src (str "/assets/themes/classic/" (:icon format))}]])
           [:span.format-label (:label format)]]])
       (:formats response))]]))

(def statistics-info
  [["activities" "Activities"]
   ["conversations" "Conversations"]
   ["domains" "Domains"]
   ["groups" "Groups"]
   ["feedSources" "Feed Sources"]
   ["feedSubscriptions" "Feed Subscriptions"]
   ["subscriptions" "Subscriptions"]
   ["users" "Users"]])


(defn statistics-line
  [stats [model-name label]]
  [:tr {:data-model model-name}
   [:td.stat-label label]
   [:td.stat-value
    (if *dynamic*
      {:data-bind (format "text: %s" model-name)}
      (get stats model-name))]])

(defn statistics-section
  [request response]
  (let [stats (actions.site/get-stats)]
    [:div.well.statistics-section (when *dynamic* {:data-bind "with: statistics"})
     [:table.table.table-compact
      [:thead
       [:tr
        [:th "Collection"]
        [:th "Count"]]]
      [:tbody
       (map (partial statistics-line stats) statistics-info)]]]))

(defn left-column-section
  [request response]
  (let [user (current-user)]
    [:aside#left-column.sidebar
     (side-navigation)
     [:hr]
     (formats-section response)
     (statistics-section request response)]))

(defn right-column-section
  [response]
  (let [user (or (:user response)
                 (current-user))]
    (list
     [:div (when *dynamic* {:data-bind "with: $root.getUser($root.targetUser() || $root.currentUser())"})
      (user-info-section user)]
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

(defn notification-line
  [message]
  [:li.alert
   [:button
    (merge {:class "close"}
           (when *dynamic*
             {:data-bind "click: $parent.dismissNotification"}))
    "x"]
   [:span
    (if *dynamic*
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
  [:div (when *dynamic* {:data-bind "with: postForm"})
   [:div (when *dynamic* {:data-bind "if: visible"})
    [:p
     (when *dynamic* {:data-bind "text: currentPage"})]
    (when (or *dynamic* (:post-form response))
      (add-form (Activity.)))]])

(defn title-section
  [request response]
  [:h1 (if *dynamic*
         {:data-bind "text: title"}
         (:title response))])

(defn main-content
  [request response]
  [:section#main
   (notification-area request response)
   (when (current-user)
     (new-post-section request response))
   (title-section request response)
   (:body response)
   ;; TODO: align middle
   [:footer.row-fluid.page-footer
    [:p "Copyright © 2011 KRONK Ltd."]
    [:p "Powered by " [:a {:href "https://github.com/duck1123/jiksnu"} "Jiksnu"]]]])

(defn navbar-search-form
  []
  [:form.navbar-search.pull-left
   {:action "/main/search" :method "post"}
   [:input.search-query.span3
    {:type "text" :placeholder "Search" :name "q"}]])


(defn fork-me-link
  []
  [:a {:href "http://github.com/duck1123/jiksnu"}
   [:img {:style "position: absolute; top: 43px; right: 0; border: 0;"
          :src "https://a248.e.akamai.net/assets.github.com/img/7afbc8b248c68eb468279e8c17986ad46549fb71/687474703a2f2f73332e616d617a6f6e6177732e636f6d2f6769746875622f726962626f6e732f666f726b6d655f72696768745f6461726b626c75655f3132313632312e706e67"
          :alt "Fork me on GitHub"}]])

(defn navbar-section
  [request response]
  [:div.navbar.navbar-fixed-top
   [:div.navbar-inner
    [:div.container-fluid
     [:a.brand.home {:href "/" :rel "top"}
      (config :site :name)]
     ;; (navbar-search-form)
     [:ul.nav.pull-right (sections.auth/login-section response)]
     [:div.navbar-text.connection-info.pull-right]
     [:div.navbar-text.pull-right
      (if *dynamic* "dynamic" "static")]]]])

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
            {:href "/opensearch/notices"
             :title "Notice Search"
             :type "application/opensearchdescription+xml"
             :rel "search"}
            {:href "/opensearch/people"
             :title "People Search"
             :type "application/opensearchdescription+xml"
             :rel "search"}
            {:href (str "http://" (config :domain) "/favicon.ico")
             :rel "shortcut icon"}])))

(defn head-section
  [request response]
  (list [:meta {:charset "UTF-8"}]
        [:title {:property "dc:title"}
         (when-not *dynamic*
           (str (when (:title response)
                  (str (:title response) " - "))
                (config :site :name)))]
        (p/include-css "/assets/styles/bootstrap.min.css"
                       "/assets/styles/bootstrap-responsive.min.css"
                       "/assets/themes/classic/standard.css"
                       ;; "/assets/js/google-code-prettify/src/prettify.css"
                       )
        (links-section request response)))

(defn scripts-section
  [request response]
  (let [websocket-path (str "ws://" (config :domain) ":" (config :http :port) "/websocket")]
    (list
    [:script {:type "text/javascript"}
     (format
      "WEB_SOCKET_SWF_LOCATION = 'WebSocketMain.swf';WEBSOCKET_PATH = '%s';var CLOSURE_NO_DEPS = true;" websocket-path)]
    (p/include-js
     "/assets/js/modernizr-2.6.1.js"
     "/assets/js/underscore-1.3.3.min.js"
     "/assets/js/jquery-1.8.0.min.js"
     "https://ajax.googleapis.com/ajax/libs/jqueryui/1.8.21/jquery-ui.min.js"
     "/assets/js/knockout-2.1.0.min.js"
     "/assets/js/bootstrap-2.4.0.min.js"
     ;; "/assets/js/google-code-prettify/src/prettify.js"
     "/assets/js/knockout.mapping-2.2.3.js"
     ;; "/assets/js/require.min.js"
     "/assets/js/backbone-0.9.2.min.js"
     "/assets/js/knockback-0.15.4.min.js"

     "/assets/js/jiksnu.js"
     )
    [:script {:type "text/javascript"}
     "goog.require('jiksnu.core');"]))

  )

(defn body-section
  [request response]
  (list
   (navbar-section request response)
   [:div.container-fluid
    #_(fork-me-link)
    [:div.row-fluid
     [:div.span2
      (left-column-section request response)]
     [:div#content.span10
      [:div.row-fluid
       (if-not (:single response)
         (list [:div.span10 (main-content request response)]
               [:div.span2 (right-column-section response)])
         [:div.span12 (main-content request response)])]]]]
   (scripts-section request response)))

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
              :prefix (->> [["foaf" ns/foaf]
                            ["dc" ns/dc]
                            ["sioc" ns/sioc]
                            ["dcterms" "http://purl.org/dc/terms/"]]
                           (map
                            (fn [[prefix uri]] (format "%s: %s" prefix uri)))
                           (string/join " "))}
             (if *dynamic*
               (when-let [vm (:viewmodel response)]
                 {:data-load-model vm})))
      [:head (head-section request response)]
      [:body (body-section request response)]]))})


(defmethod apply-template :html
  [request response]
  (merge response
         (if (not= (:template response) false)
           (page-template-content request
                                  (if (:flash request)
                                    (assoc response :flash (:flash request))
                                    response)))))

(defmethod apply-template :command
  [request response]
  (let [body (:body response)]
    (assoc response :body
           {:type (get response :type "event")
            :body body})))
