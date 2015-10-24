(ns jiksnu.modules.web.sections.layout-sections
  (:require [ciste.core :refer [apply-template]]
            [ciste.config :refer [config environment]]
            [clojure.string :as string]
            [clojure.tools.logging :as log]
            [hiccup.core :as h]
            [hiccup.page :as p]
            [jiksnu.actions.site-actions :as actions.site]
            [jiksnu.actions.subscription-actions :as actions.subscription]
            [jiksnu.modules.web.actions.core-actions :as actions.web.core]
            [jiksnu.modules.web.sections :refer [bind-to]]
            [jiksnu.modules.web.sections.activity-sections :as sections.activity]
            [jiksnu.modules.web.sections.auth-sections :as sections.auth]
            [jiksnu.modules.web.sections.user-sections :as sections.user]
            [jiksnu.namespace :as ns]
            [jiksnu.session :as session])
  (:import jiksnu.model.Activity
           jiksnu.model.User))

(defn user-info-section
  [user]
  (list
   ))

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

(defn notification-line
  [message]
  [:li.alert
   [:button {:class "close"
             :data-bind "click: $parent.dismissNotification"}
    "x"]
   [:span {:data-bind "text: message"}]])

(defn notification-area
  [request response]
  [:div#flash
   [:ul.unstyled
    {:data-bind "foreach: notifications"}
    (notification-line nil)]])

(defn navbar-search-form
  []
  [:form.navbar-search.pull-left
   {:action "/main/search" :method "post"}
   [:input.search-query.col-md-3
    {:type "text" :placeholder "Search" :name "q"}]])

(defn get-prefixes
  []
  (->> [["foaf" ns/foaf]
        ["dc" ns/dc]
        ["sioc" ns/sioc]
        ["dcterms" "http://purl.org/dc/terms/"]]
       (map
        (fn [[prefix uri]] (format "%s: %s" prefix uri)))
       (string/join " ")))

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
            {:href (str "//" (config :domain) "/favicon.ico")
             :rel "shortcut icon"}])))

(defonce scripts-section-hook (ref []))

(defn style-section
  []
  (p/include-css
   "/vendor/bootstrap/dist/css/bootstrap.min.css"
   "/vendor/datatables/media/css/jquery.dataTables.min.css"
   "/vendor/angular-busy/dist/angular-busy.min.css"
   "/vendor/angular-datatables/dist/plugins/bootstrap/datatables.bootstrap.min.css"
   "/vendor/angular-notify/dist/angular-notify.min.css"
   "/vendor/angular-ui/build/angular-ui.min.css"
   "/vendor/angular-cfp-hotkeys/build/hotkeys.min.css"
   "/css/standard.css"))

(defn scripts-section
  [request response]
  (let [websocket-path (str "wss://" (config :domain) "/")]
    (list
     [:script {:type "text/javascript"}
      ;; "WEB_SOCKET_SWF_LOCATION = 'WebSocketMain.swf';"
      (format "WEBSOCKET_PATH = '%s';" websocket-path)
      "var CLOSURE_NO_DEPS = true;"]
     (p/include-js
      "/vendor/jquery/dist/jquery.min.js"
      "/vendor/angular/angular.min.js"
      "/vendor/angular-datatables/dist/angular-datatables.min.js"
      "/vendor/momentjs/min/moment.min.js"
      "/vendor/underscore/underscore-min.js"
      "/vendor/showdown/src/showdown.js"
      "/vendor/datatables/media/js/jquery.dataTables.min.js"
      "/vendor/angularjs-geolocation/dist/angularjs-geolocation.min.js"
      "/vendor/angular-busy/dist/angular-busy.min.js"
      "/vendor/js-data/dist/js-data.min.js"
      "/vendor/js-data-angular/dist/js-data-angular.min.js"
      "/vendor/angular-file-upload/angular-file-upload.min.js"
      "/vendor/angular-google-maps/dist/angular-google-maps.min.js"
      "/vendor/angular-cfp-hotkeys/build/hotkeys.min.js"
      "/vendor/angular-paginate-anything/src/paginate-anything.js"
      "/vendor/angular-markdown-directive/markdown.js"
      "/vendor/angular-moment/angular-moment.min.js"
      "/vendor/angular-notify/dist/angular-notify.min.js"
      "/vendor/angular-sanitize/angular-sanitize.min.js"
      "/vendor/angular-bootstrap/ui-bootstrap-tpls.min.js"
      "/vendor/angular-ui-router/release/angular-ui-router.min.js"
      "/vendor/angular-validator/dist/angular-validator.min.js"
      "/vendor/angular-ws/angular-ws.min.js"
      "/cljs/jiksnu.js")
     (doall (map (fn [hook]
                   (hook request response))
                 @scripts-section-hook))
     [:script {:type "text/javascript"}
      "goog.require('jiksnu.core');"])))

(defn right-column-section
  []
  (let [user (User.)]
    [:h3 "Right column"]
    #_(list
       (bind-to "$root.targetUser() || $root.currentUser()"
                (user-info-section user))
       (:aside response))))

(defn page-template-content
  [request response]
  (p/html5
   {:ng-app "jiksnu"}
   [:head
    [:meta {:charset "UTF-8"}]
    [:meta {:http-equiv "X-UA-Compatible" :content "IE=edge"}]
    [:meta {:name "viewport" :content "width=device-width, initial-scale=1.0"}]
    [:base {:href "/"}]
    [:title {:property "dc:title"} (config :site :name)]
    (style-section)
    (links-section request response)
    (scripts-section request response)]
   [:body
    [:nav-bar]
    [:div.container
     ;; [:a.visible-sm.visible-xs {:href "#mainNav"} "Jump to Nav"]
     [:div.row
      #_[:left-column.col-sm-2]
      [:div.col-sm-12
       [:add-post-form.center]
       [:h1 {:data-bind "text: title"}]
       [:div {:ui-view ""}]]
      #_[:right-column.col-sm-2]

      ]]
    [:footer.row.page-footer
     [:p "Copyright © 2011-2015 KRONK Ltd."]
     [:p "Powered by "
      [:a {:href "https://github.com/duck1123/jiksnu"}
       "Jiksnu"]]]]))

(defmethod apply-template :html
  [request response]
  (merge response
         (if (not= (:template response) false)
           (page-template-content request
                                  (if (:flash request)
                                    (assoc response :flash (:flash request))
                                    response)))))
