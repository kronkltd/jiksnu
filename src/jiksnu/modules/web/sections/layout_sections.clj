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
   (show-section user)
   [:div {:data-model "user"}
    (sections.subscription/subscriptions-widget user)
    (sections.subscription/subscribers-widget user)
    (sections.group/user-groups user)
    (with-sub-page "streams"
      [:streams-widget])
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

;; TODO: this will be dynamically included
(defn top-users
  []
  [:div
   [:p "Users with most posts"]
   [:ul
    [:li [:a {:href "#"} "#"]]]])

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
      ;; "/webjars/jquery/2.1.3/jquery.min.js"
      "/webjars/angularjs/1.3.8/angular.min.js"
      "/webjars/angular-ui-bootstrap/0.12.0/ui-bootstrap-tpls.min.js"
      "/webjars/angular-ui-router/0.2.13/angular-ui-router.min.js"
      "/webjars/angular-moment/0.8.2-1/angular-moment.min.js"
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
        (str (when (:title response)
               (str (:title response) " - "))
             (config :site :name))]
       (style-section)
       (links-section request response)
       (scripts-section request response)]
      [:body
       [:div {:ui-view "navbar"}]
       [:div.container-fluid
        [:a.visible-sm.visible-xs {:href "#mainNav"} "Jump to Nav"]
        [:div.row
         [:div.col-sm-2 {:ui-view "leftColumn"}]
         [:div.col-sm-8
          [:section
           [:div {:ui-view "newPost"}]
           #_(new-post-section request response)
           [:h1 {:data-bind "text: title"}]
           (:body response)]]
         [:div.col-sm-2 {:ui-view "rightColumn"}]]]
       [:footer.row.page-footer
        [:p "Copyright © 2011 KRONK Ltd."]
        [:p "Powered by " [:a {:href "https://github.com/duck1123/jiksnu"}
                           "Jiksnu"]]]
      ]]))})

(defmethod apply-template :html
  [request response]
  (merge response
         (if (not= (:template response) false)
           (page-template-content request
                                  (if (:flash request)
                                    (assoc response :flash (:flash request))
                                    response)))))
