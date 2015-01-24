(ns jiksnu.modules.web.sections.layout-sections
  (:require [ciste.core :refer [apply-template]]
            [ciste.config :refer [config environment]]
            [ciste.sections.default :refer [show-section]]
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
            [jiksnu.modules.web.sections.subscription-sections :as sections.subscription]
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
            {:href (str "http://" (config :domain) "/favicon.ico")
             :rel "shortcut icon"}])))

(defonce scripts-section-hook (ref []))

(defn style-section
  []
  (p/include-css
   "/webjars/bootstrap/css/bootstrap.min.css"
   "/webjars/datatables/css/jquery.dataTables.min.css"
   "/webjars/angular-busy/angular-busy.min.css"
   ;; angular-cache
   "/webjars/angular-datatables/datatables.bootstrap.min.css"
   ;; angular-file-upload
   ;; angular-goolge-maps
   "/webjars/angular-hotkeys/hotkeys.min.css"
   ;; angular-paginate-anything
   ;; angular-markdown-directive
   ;; angular-moment
   "/webjars/angular-notify/angular-notify.min.css"
   "/webjars/angular-ui/angular-ui.min.css"
   ;; angular-ui-bootstrap
   ;; angular-ui-router
   ;; angular-validator
   ;; angular-ws
   "/css/standard.css"))

(defn scripts-section
  [request response]
  (let [websocket-path (str "ws://" (config :domain) "/")]
    (list
     [:script {:type "text/javascript"}
      ;; "WEB_SOCKET_SWF_LOCATION = 'WebSocketMain.swf';"
      (format "WEBSOCKET_PATH = '%s';" websocket-path)
      "var CLOSURE_NO_DEPS = true;"]
     (p/include-js
      ;; "/webjars/underscorejs/1.7.0/underscore-min.js"
      "/webjars/momentjs/min/moment.min.js"
      "/webjars/jquery/jquery.min.js"
      "/webjars/underscorejs/underscore-min.js"
      "/webjars/showdown/src/showdown.js"
      "/webjars/datatables/js/jquery.dataTables.min.js"
      "/webjars/angularjs/angular.min.js"
      "/webjars/angularjs-geolocation/angularjs-geolocation.min.js"
      "/webjars/angular-busy/angular-busy.min.js"
      "/webjars/angular-cache/angular-cache.min.js"
      "/webjars/angular-data/angular-data.js"
      "/webjars/angular-datatables/angular-datatables.min.js"
      "/webjars/angular-file-upload/angular-file-upload.min.js"
      "/webjars/angular-google-maps/angular-google-maps.min.js"
      "/webjars/angular-hotkeys/hotkeys.min.js"
      "/webjars/angular-paginate-anything/paginate-anything.js"
      "/webjars/angular-markdown-directive/markdown.js"
      "/webjars/angular-moment/angular-moment.min.js"
      "/webjars/angular-notify/angular-notify.min.js"
      "/webjars/angular-sanitize/angular-sanitize.min.js"
      "/webjars/angular-ui-bootstrap/ui-bootstrap-tpls.min.js"
      "/webjars/angular-ui-router/angular-ui-router.min.js"
      "/webjars/angular-validator/angular-validator.min.js"
      "/webjars/angular-ws/angular-ws.min.js"
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
   {:xmlns:sioc ns/sioc
    :xmlns:dc ns/dc
    :xmlns:foaf ns/foaf
    :xmlns:dcterms ns/dcterms
    :lang "en"
    :xml:lang "en"
    :ng-app "jiksnu"
    ;; :ng-strict-di ""
    :prefix (get-prefixes)}
   [:head
    [:meta {:charset "UTF-8"}]
    [:meta {:http-equiv "X-UA-Compatible" :content "IE=edge"}]
    [:meta {:name "viewport"
            :content "width=device-width, initial-scale=1.0"}]
    [:meta {:name "google-site-verification" :content "d4_Ko7ZhJ7AWe6G1MdxPvlqK6DQMtlWGuwquq9of0l4"}]
    [:base {:href "/"}]
    [:title {:property "dc:title"} (config :site :name)]
    (style-section)
    (links-section request response)
    (scripts-section request response)]
   [:body
    [:div.container
     [:nav-bar]
     ;; [:a.visible-sm.visible-xs {:href "#mainNav"} "Jump to Nav"]
     [:div.row
      [:div.col-sm-2 {:left-column ""}]
      [:div.col-sm-8
       [:div.row {:add-post-form ""}]
       [:div.row
        [:h1 {:data-bind "text: title"}]
        [:div {:ui-view ""}]]]
      [:div.col-sm-2 {:right-column ""}]]]
    [:footer.row.page-footer
     [:p "Copyright © 2011 KRONK Ltd."]
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
