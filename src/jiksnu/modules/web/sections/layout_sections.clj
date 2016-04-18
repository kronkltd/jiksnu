(ns jiksnu.modules.web.sections.layout-sections
  (:require [ciste.core :refer [apply-template]]
            [ciste.config :refer [config config* environment]]
            [clojure.string :as string]
            [hiccup.page :as p]
            [jiksnu.modules.web.sections :refer [bind-to]]
            [jiksnu.namespace :as ns]))

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
   "/vendor/highlightjs/styles/github.css"
   "/vendor/datatables/media/css/jquery.dataTables.min.css"
   "/vendor/angular-busy/dist/angular-busy.min.css"
   "/vendor/angular-datatables/dist/plugins/bootstrap/datatables.bootstrap.min.css"
   "/vendor/angular-ui/build/angular-ui.min.css"
   "/vendor/angular-ui-notification/dist/angular-ui-notification.min.css"
   "/vendor/angular-ui-select/dist/select.min.css"
   "/vendor/angular-cfp-hotkeys/build/hotkeys.min.css"
   "/css/standard.css"))

(defn scripts-section
  [request response]
  (list
   [:script {:type "text/javascript"} "var CLOSURE_NO_DEPS = true;"]
   (p/include-js
    "/vendor/jquery/dist/jquery.min.js"
    "/vendor/angular/angular.min.js"
    "/vendor/angular-datatables/dist/angular-datatables.min.js"
    "/vendor/highlightjs/highlight.pack.js"
    "/vendor/moment/min/moment.min.js"
    "/vendor/underscore/underscore-min.js"
    "/vendor/showdown/src/showdown.js"
    "/vendor/datatables/media/js/jquery.dataTables.min.js"
    "/vendor/angularjs-geolocation/dist/angularjs-geolocation.min.js"
    "/vendor/angular-busy/dist/angular-busy.min.js"
    "/vendor/js-data/dist/js-data.min.js"
    "/vendor/js-data-angular/dist/js-data-angular.min.js"
    "/vendor/angular-file-upload/dist/angular-file-upload.min.js"
    "/vendor/angular-cfp-hotkeys/build/hotkeys.min.js"
    "/vendor/angular-clipboard/angular-clipboard.js"
    "/vendor/angular-highlightjs/angular-highlightjs.js"
    "/vendor/angular-markdown-directive/markdown.js"
    "/vendor/angular-moment/angular-moment.min.js"
    "/vendor/angular-sanitize/angular-sanitize.min.js"
    "/vendor/angular-bootstrap/ui-bootstrap-tpls.min.js"
    "/vendor/angular-ui-notification/dist/angular-ui-notification.min.js"
    "/vendor/angular-ui-router/release/angular-ui-router.min.js"
    "/vendor/angular-ui-select/dist/select.min.js"
    "/vendor/angular-validator/dist/angular-validator.min.js"
    "/vendor/angular-websocket/angular-websocket.min.js"
    "/vendor/raven-js/dist/raven.js"
    "/vendor/raven-js/dist/plugins/angular.js"
    "/cljs/jiksnu.js")
   (map #(% request response) @scripts-section-hook)
   (when-let [dsn (config* :sentry :dsn :client)]
     [:script {:type "text/javascript"}
      (str "SENTRY_DSN_CLIENT=\"" dsn "\"")])))

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
      #_[:right-column.col-sm-2]]]
    [:footer.row.page-footer
     [:p "Copyright © 2011-2015 KRONK Ltd."]
     [:p "Powered by "
      [:a {:href "https://github.com/duck1123/jiksnu"}
       "Jiksnu"]]
     [:p
      [:a {:href (str "/vendor/swagger-ui/dist/index.html?url=https://"
                      (config :domain)
                      "/api-docs.json")
           :target "_top"}
       "API"]]]]))

(defmethod apply-template :html
  [request response]
  (merge response
         (if (not= (:template response) false)
           (page-template-content request
                                  (if (:flash request)
                                    (assoc response :flash (:flash request))
                                    response)))))
