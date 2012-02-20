(ns jiksnu.view
  (:use (ciste core
               [config :only [config]]
               [debug :only [spy]]
               formats sections views)
        ciste.sections.default
        (jiksnu model session)
        jiksnu.sections.layout-sections)
  (:require (clj-tigase [core :as tigase])
            (hiccup [core :as h])
            (jiksnu [namespace :as ns]
                    [xmpp :as xmpp])
            (jiksnu.sections [auth-sections :as sections.auth])
            (jiksnu.xmpp [element :as element])
            (plaza.rdf [core :as rdf])
            (plaza.rdf.vocabularies [foaf :as foaf])))

(defsection link-to :default
  [record & options]
  (let [options-map (apply hash-map options)]
    [:a
     (apply merge {:href (uri record)} options-map)
     [:span {:about (uri record)
             :property "dc:title"}
      (or (:title options-map) (title record))] ]))

(defn include-script
  [src]
  [:script {:type "text/javascript"
            :src src}])

(defn main-content
  [response]
  (list (when (:title response)
          [:h1 (:title response)])
        (when (:flash response)
          [:div#flash (:flash response)])
        (:body response)))

(defn page-template-content
  [response]
  {:headers {"Content-Type" "text/html; charset=utf-8"}
   :body
   (str
    "<!doctype html>\n"
    (h/html
     [:html
      {:xmlns:sioc ns/sioc
       :xmlns:dc ns/dc
       :xmlns:foaf ns/foaf
       :xmlns:dcterms ns/dcterms
       :prefix "foaf: http://xmlns.com/foaf/0.1/
                dc: http://purl.org/dc/elements/1.1/
                sioc: http://rdfs.org/sioc/ns#
                dcterms: http://purl.org/dc/terms/"}
      [:head
       [:meta {:charset "utf-8"}]
       [:title
        (when (:title response)
          (str (:title response) " - "))
        (config :site :name)]
       [:link {:type "text/css"
               :href "/bootstrap/css/bootstrap.css"
               :rel "stylesheet"
               :media "screen"}]
       [:link {:type "text/css"
               :href "/themes/classic/standard.css"
               :rel "stylesheet"
               :media "screen"}]]
      [:body
       [:div.navbar.navbar-fixed-top
        [:div.navbar-inner
         [:div.container
          [:a.brand.home {:href "/"} (config :site :name)]
          (navigation-section response)
          [:ul.nav.pull-right (sections.auth/login-section response)]]]]
       [:div.container
        [:div.row
         [:div.span2 (left-column-section response)]
         [:div#content.span10
          [:div#notification-area.row
           [:div.span10 (devel-warning response)]]
          [:div.row
           (if-not (:single response)
             (list [:div.span7 (main-content response)]
                   [:div.span3 (right-column-section response)])
             [:div.span10 (main-content response)])]]]
        [:footer.row
         [:p "Copyright © 2011 KRONK Ltd."]
         [:p "Powered by " [:a {:href "https://github.com/duck1123/jiksnu"} "Jiksnu"]]
         ]]
       ;; (include-script "/cljs/bootstrap.js")
       (include-script "http://code.jquery.com/jquery-1.7.1.js")
       (include-script "/bootstrap/js/bootstrap.js")
       (include-script "https://browserid.org/include.js")
       #_[:script {:type "text/javascript"}
        "goog.require('jiksnu.core');"]]]))})

(defmethod apply-template :html
  [request response]
  (merge response
         (if (not= (:template response) false)
           (page-template-content
            (if (:flash request)
              (assoc response :flash (:flash request))
              response)))))

(defmethod apply-view-by-format :atom
  [request response])

(defmethod format-as :n3
  [request format response]
  (-> response 
      (assoc :body (-> response :body
                       rdf/model-add-triples
                       rdf/defmodel
                       (rdf/model-to-format :n3)
                       with-out-str))
      (assoc-in [:headers "Content-Type"] "text/n3; charset=utf-8")))

(defmethod format-as :xml
  [format request response]
  (-> response
      (assoc-in [:headers "Content-Type"] "application/xml")
      (assoc :body (h/html (:body response)))))

(defmethod serialize-as :http
  [serialization response-map]
  (assoc-in
   (merge {:status 200} response-map)
   [:headers "Content-Type"]
   (or (-> response-map :headers (get "Content-Type"))
       "text/html; charset=utf-8")))

(defmethod serialize-as :xmpp
  [serialization response]
  (if response
    (tigase/make-packet response)))
