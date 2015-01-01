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
            [jiksnu.modules.web.actions.core-actions :as actions.web.core]
            [jiksnu.modules.web.sections :refer [bind-to pagination-links with-sub-page]]
            [jiksnu.modules.web.sections.activity-sections :as sections.activity]
            [jiksnu.modules.web.sections.auth-sections :as sections.auth]
            [jiksnu.modules.web.sections.group-sections :as sections.group]
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
   "/webjars/bootstrap/3.3.1/css/bootstrap.min.css"
   "/webjars/angular-hotkeys/1.4.0/hotkeys.min.css"
   "/webjars/angular-notify/2.0.2/angular-notify.min.css"
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
      "/webjars/momentjs/2.8.3/min/moment.min.js"
      "/webjars/jquery/2.1.3/jquery.min.js"
      "/webjars/underscorejs/1.7.0/underscore-min.js"
      "/webjars/angularjs/1.3.8/angular.min.js"
      "/webjars/angular-file-upload/2.0.5/angular-file-upload.min.js"
      "/webjars/angularjs-geolocation/0.1.1/angularjs-geolocation.min.js"
      "/webjars/angular-ui-bootstrap/0.12.0/ui-bootstrap-tpls.min.js"
      "/webjars/angular-ui-router/0.2.13/angular-ui-router.min.js"
      "/webjars/angular-moment/0.8.2-1/angular-moment.min.js"
      "/webjars/angular-validator/0.2.5/angular-validator.min.js"
      "/webjars/angular-cache/3.0.2/angular-cache.min.js"
      "/webjars/angular-google-maps/2.0.11/angular-google-maps.min.js"
      "/webjars/angular-hotkeys/1.4.0/hotkeys.min.js"
      "/webjars/angular-markdown-directive/0.3.0/markdown.js"
      "/webjars/angular-notify/2.0.2/angular-notify.min.js"
      "/webjars/angular-sanitize/1.2.16/angular-sanitize.min.js"
      "/webjars/angular-ws/1.1.0/angular-ws.min.js"
      "/webjars/showdown/0.3.1/src/showdown.js"
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
          "Jiksnu"]]]]]))})

(defmethod apply-template :html
  [request response]
  (merge response
         (if (not= (:template response) false)
           (page-template-content request
                                  (if (:flash request)
                                    (assoc response :flash (:flash request))
                                    response)))))
