(ns jiksnu.modules.web.sections.layout-sections
  (:require [ciste.core :refer [apply-template]]
            [ciste.config :refer [config config*]]
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
   "/vendor/datatables.net-dt/css/jquery.dataTables.css"
   "/vendor/angular-busy/dist/angular-busy.min.css"
   "/vendor/angular-datatables/dist/plugins/bootstrap/datatables.bootstrap.min.css"
   "/vendor/angular-ui-notification/dist/angular-ui-notification.min.css"
   "/vendor/ui-select/dist/select.min.css"
   "/vendor/angular-hotkeys/build/hotkeys.min.css"
   "/css/standard.css"))

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
    [:script {:type "text/javascript"} "var CLOSURE_NO_DEPS = true;"]
    (p/include-js "/main.js" "/cljs/jiksnu.js")
    (map #(% request response) @scripts-section-hook)
    (when-let [dsn (config* :sentry :dsn :client)]
      [:script {:type "text/javascript"}
       (str "SENTRY_DSN_CLIENT=\"" dsn "\"")])]
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
     [:p "Copyright © 2011-2016 KRONK Ltd."]
     [:p "Powered by "
      [:a {:href "https://github.com/kronkltd/jiksnu"}
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
