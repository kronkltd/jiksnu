(ns jiksnu.modules.web.sections.layout-sections
  (:require [cemerick.austin.repls]
            [ciste.core :refer [apply-template]]
            [ciste.config :refer [config environment]]
            [ciste.sections.default :refer [add-form show-section]]
            [clojure.string :as string]
            [clojure.tools.logging :as log]
            [hiccup.core :as h]
            [hiccup.page :as p]
            [jiksnu.actions.site-actions :as actions.site]
            [jiksnu.actions.subscription-actions :as actions.subscription]
            [jiksnu.ko :refer [*dynamic*]]
            [jiksnu.modules.web.actions.core-actions :as actions.web.core]
            [jiksnu.modules.web.sections :refer [bind-to display-property
                                                 pagination-links with-sub-page]]
            [jiksnu.modules.web.sections.activity-sections :as sections.activity]
            [jiksnu.modules.web.sections.auth-sections :as sections.auth]
            [jiksnu.modules.web.sections.group-sections :as sections.group]
            [jiksnu.modules.web.sections.stream-sections :as sections.stream]
            [jiksnu.modules.web.sections.subscription-sections :as sections.subscription]
            [jiksnu.modules.web.sections.user-sections :as sections.user]
            [jiksnu.namespace :as ns]
            [jiksnu.session :as session])
  (:import jiksnu.model.Activity
           jiksnu.model.User))

(defn user-info-section
  [user]
  (list
   (show-section user)
   [:div {:data-model "user"}
    (sections.subscription/subscriptions-widget user)
    (sections.subscription/subscribers-widget user)
    (sections.group/user-groups user)
    (with-sub-page "streams"
      (sections.stream/streams-widget user))
    (sections.group/groups-widget user)]))

(defn navigation-group
  [group]
  (concat [[:li.nav-header (:title group)]]
          (map
           (fn [link]
             [:li [:a
                   (if-let [state (:state link)]
                     {:ui-sref state}
                     {:href (:href link)})
                   (:title link)]])
           (:items group))))

(defn side-navigation
  []
  [:ul.nav.nav-list
   {:ng-controller "NavController"}
   [:li {:ng-repeat "item in items"}
    [:a {:href "{{item.href}}"} "{{item.label}}"]
    ]
   (->> (actions.web.core/nav-info)
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
  []
  [:div
   [:h3 "Formats"]
   [:ul.unstyled
    [:li.format-line
     {:ng-repeat "format in formats"}
     [:a {:href "{{format.href}}"}
      [:span.format-icon
       [:img {:alt ""
              :ng-src "/themes/classic/{{format.icon}}"}]]
      [:span.format-label "{{format.label}}"]]]]])

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
    {:data-bind "foreach: notifications"}
    (notification-line nil)]])

(defn new-post-section
  [request response]
  [:div {:ng-controller "NewPostController"}
   [:div {:ng-if "postForm.visible"}
    (add-form (Activity.))]])

(defn title-section
  [request response]
  [:h1 {:data-bind "text: title"}])

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
   [:input.search-query.col-md-3
    {:type "text" :placeholder "Search" :name "q"}]])

(defn style-section
  []
  (let [theme (config :site :theme)]
    (p/include-css
     (if (= theme "classic")
       "/webjars/bootstrap/3.3.0/css/bootstrap.min.css"
       (format "http://bootswatch.com/%s/bootstrap.min.css" theme))
     "/css/standard.css"
     )))

(defn get-prefixes
  []
  (->> [["foaf" ns/foaf]
        ["dc" ns/dc]
        ["sioc" ns/sioc]
        ["dcterms" "http://purl.org/dc/terms/"]]
       (map
        (fn [[prefix uri]] (format "%s: %s" prefix uri)))
       (string/join " ")))

(defn navbar-expand-button
  [target desc]
  [:button.navbar-toggle
   {:type "button"
    :data-toggle "collapse"
    :data-target target}
   [:span.sr-only desc]
   [:span.icon-bar]
   [:span.icon-bar]
   [:span.icon-bar]])

(defn navbar-section
  [request response]
  [:nav.navbar.navbar-default.navbar-inverse
   {:role "navigation"
    :ng-controller "NavBarController"
    :ui-view "navbar"}
   [:div.container-fluid
    [:div.navbar-header
     (navbar-expand-button "#main-navbar-collapsw-1" "Toggle Navigation")
     [:a.navbar-brand.home {:href "/" :rel "top"}
      "{{app.name}}"]]
    [:div#main-navbar-collapse-1.navbar-collapse.collapse
     [::ul.nav.navbar-nav.navbar-right {:ng-if "app.user"}
      [:li.dropdown
       [:a.dropdown-toggle (merge {:href "#" :data-toggle "dropdown"})
        [:span
         [:span {:data-model "user"}
          [:show-avatar {:data-id "{{app.user}}"}]
          "{{app.user}}"
          [:link-to {:data-id "{{app.user}}" :data-model "user"}]]]
        [:b.caret]]
       [:ul.dropdown-menu
        [:li
         [:a.settings-link {:href "/main/settings"} "Settings"]
         [:a.profile-link {:href "/main/profile"} "Profile"]
         [:a.logout-link {:href "/main/logout?_method=POST"
                          :target "_self"} "Log out"]]]]]
     [::ul.nav.navbar-nav.navbar-right {:ng-if "!app.user"}
      [:li.unauthenticated [:a.login-link {:ui-sref "loginPage"} "Login"]]
      [:li.divider-vertical]
      [:li [:a.register-link {:href "/main/register"} "Register"]]]]]])

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
            ;; {:href "/opensearch/notices"
            ;;  :title "Notice Search"
            ;;  :type "application/opensearchdescription+xml"
            ;;  :rel "search"}
            ;; {:href "/opensearch/people"
            ;;  :title "People Search"
            ;;  :type "application/opensearchdescription+xml"
            ;;  :rel "search"}
            {:href (str "http://" (config :domain) "/favicon.ico")
             :rel "shortcut icon"}])))

(defonce scripts-section-hook (ref []))

(defn scripts-section
  [request response]
  (let [websocket-path (str "ws://" (config :domain) "/")]
    (list
     [:script {:type "text/javascript"}
      ;; "WEB_SOCKET_SWF_LOCATION = 'WebSocketMain.swf';"
      (format "WEBSOCKET_PATH = '%s';" websocket-path)
      "var CLOSURE_NO_DEPS = true;"]
     (p/include-js
      ;; TODO: Pull the version numbers out, load from
      "/webjars/underscorejs/1.7.0/underscore-min.js"
      "/webjars/jquery/2.1.1/jquery.min.js"
      "/webjars/knockout/3.2.0/knockout.debug.js"
      "/webjars/bootstrap/3.3.0/js/bootstrap.min.js"
      "/js/bootstrap-markdown/1.0.0/js/bootstrap-markdown.js"
      "/webjars/backbonejs/1.1.2/backbone-min.js"
      "/js/supermodel/0.0.4/supermodel.js"
      "/js/knockback/0.17.2/knockback.js"
      "/webjars/angularjs/1.3.0/angular.min.js"
      "/webjars/angularjs/1.3.0/angular-route.min.js"
      "/webjars/angular-ui/0.4.0/angular-ui.min.js"
      "/webjars/angular-ui-bootstrap/0.12.0/ui-bootstrap.min.js"
      "/webjars/angular-ui-router/0.2.13/angular-ui-router.min.js"
      "/webjars/angular-moment/0.8.2-1/angular-moment.min.js"
      "/cljs/jiksnu.js")
     (doall
      (map (fn [hook]
             (hook request response))
           @scripts-section-hook))
     [:script {:type "text/javascript"}
      "goog.require('jiksnu.core');"]
     (if (= "true" (:repl (:params request)))
       [:script (cemerick.austin.repls/browser-connected-repl-js)]))))

(defn right-column-section
  []
  (let [user (User.)]
    [:h3 "Right column"]
    #_(list
     (bind-to "$root.targetUser() || $root.currentUser()"
       (user-info-section user))
     (:aside response))))

(defn main-content
  [request response]
  [:section#main
   (when (session/current-user)
     (new-post-section request response))
   (title-section request response)
   (:body response)])

(defn left-column-section
  []
  (list
   [:div#mainNav
    (side-navigation)]
   [:hr]
   (formats-section)))

(defn page-template-content
  [request response]
  {:headers {"Content-Type" "text/html; charset=utf-8"}
   :body
   (str
    "<!DOCTYPE html" ">"
    (h/html
     [:html
      {:xmlns:sioc ns/sioc
       :xmlns:dc ns/dc
       :xmlns:foaf ns/foaf
       :xmlns:dcterms ns/dcterms
       ;; :version "HTML+RDFa 1.1"
       :lang "en"
       :xml:lang "en"
       :ng-app "jiksnuApp"
       :prefix (get-prefixes) }
      [:head
       [:meta {:charset "UTF-8"}]
       [:meta {:http-equiv "X-UA-Compatible" :content "IE=edge"}]
       [:meta {:name "viewport"
               :content "width=device-width, initial-scale=1.0"}]
       [:base {:href "/"}]
       [:title {:property "dc:title"}
        (when-not *dynamic*
          (str (when (:title response)
                 (str (:title response) " - "))
               (config :site :name)))]
       (style-section)
       (links-section request response)]
      [:body
       ;; {:ng-controller "AppController"}
       (navbar-section request response)
       [:div.container-fluid
        [:a.visible-sm.visible-xs {:href "#mainNav"} "Jump to Nav"]
        [:div.row
         [:div#content.col-sm-10.col-sm-push-2
          [:div.row
           (if-not (:single response)
             (list [:div.col-md-10 (main-content request response)]
                   [:div.col-md-2 {:ui-view "rightColumn"}
                    #_(right-column-section response)])
             [:div.col-md-12 (main-content request response)])]]


         [:div.col-sm-2.col-sm-pull-10
          [:aside#left-column.sidebar {:ui-view "leftColumn"}
           ;; (left-column-section)
           ]]]
        [:footer.row.page-footer
         [:p "Copyright © 2011 KRONK Ltd."]
         [:p "Powered by " [:a {:href "https://github.com/duck1123/jiksnu"}
                            "Jiksnu"]]]]
       (scripts-section request response)]]))})


(defmethod apply-template :html
  [request response]
  (merge response
         (if (not= (:template response) false)
           (page-template-content request
                                  (if (:flash request)
                                    (assoc response :flash (:flash request))
                                    response)))))
