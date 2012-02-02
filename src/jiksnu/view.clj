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
          (sections.auth/login-section response)]]]
       [:div#wrap.container
        #_[:div.subnav.subnav-fixed
         [:ul.nav.nav-pills
          [:a {:href "/admin"} "Admin"]]]
        [:div#notification-area.row
         (when (:flash response)
           [:div#flash (:flash response)])]
        [:div#site-main.row
         [:div.span2
          (left-column-section response nil nil nil nil)]
         [:div#content.span7
          (devel-warning response)
          (when (:title response)
            [:h1 (:title response)])
          (:body response)]
         [:div.span3
          "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Pellentesque volutpat dui vel erat interdum ac porta quam pulvinar. Vestibulum cursus arcu sit amet odio placerat imperdiet. Donec id enim leo. Cras at interdum velit. Sed hendrerit sapien odio. Nam a diam leo, et pellentesque turpis. Fusce suscipit iaculis dignissim. Aenean et augue diam, convallis rhoncus risus. Sed quis lorem vitae libero dictum tincidunt ut at leo. Donec quis sapien vel leo adipiscing interdum eget vitae turpis.

Donec imperdiet felis ut ligula congue ultricies auctor mauris congue. In consectetur leo in turpis viverra volutpat. Maecenas eu lobortis magna. Duis ornare sem in sem tristique vitae ullamcorper nunc interdum. Curabitur laoreet lectus vel nibh rutrum volutpat. Morbi tempus tristique placerat. In dignissim odio nec nunc adipiscing tristique. Praesent sit amet dui eget nunc iaculis condimentum quis ut mauris. Etiam lobortis adipiscing diam. Etiam neque ligula, facilisis vel bibendum quis, placerat quis erat.

Nullam et rutrum neque. Vestibulum ante ipsum primis in faucibus orci luctus et ultrices posuere cubilia Curae; Nunc sit amet lorem quam, a rutrum justo. Vestibulum ante ipsum primis in faucibus orci luctus et ultrices posuere cubilia Curae; Cras in neque massa. Etiam pellentesque, leo nec eleifend elementum, felis libero elementum risus, ut sagittis sem urna id risus. Curabitur ut hendrerit lectus. Aenean hendrerit arcu ligula, tristique congue ante. In arcu nibh, semper vitae interdum vel, facilisis id dolor. Aliquam pretium, metus ut varius pretium, neque mi semper nulla, eget porttitor nulla libero nec tellus. Maecenas facilisis leo ut magna tincidunt in dictum est consequat. Ut a ligula et enim auctor imperdiet vel non nunc.

Phasellus pulvinar turpis non massa aliquam ultricies. Nulla tristique nulla in justo imperdiet id malesuada ipsum cursus. Nunc pharetra adipiscing tellus, a porta sem malesuada interdum. Pellentesque laoreet ante eu elit condimentum molestie. Class aptent taciti sociosqu ad litora torquent per conubia nostra, per inceptos himenaeos. Duis gravida augue at nisi ullamcorper tempor. Sed convallis, nulla aliquam malesuada mattis, purus urna condimentum nisl, at malesuada magna velit ac est. Nunc at accumsan magna. Curabitur aliquam nunc et dui lacinia varius. Sed pellentesque, odio non porttitor ornare, purus sem mattis dui, vel tempor enim erat a magna.

Suspendisse lobortis mi a lorem interdum nec faucibus magna suscipit. Praesent suscipit lacinia congue. Praesent commodo tempor lacus, a tempor metus blandit quis. Cras nec ante a ligula convallis lobortis. In at dolor non magna interdum gravida. Maecenas id erat nisi. Curabitur pretium auctor nibh, in posuere tortor dignissim vel. "]]
        [:footer.row
         [:p "Copyright © 2011 KRONK Ltd."]]]
       (include-script "/cljs/bootstrap.js")
       (include-script "http://code.jquery.com/jquery-1.5.2.min.js")
       (include-script "/bootstrap/js/bootstrap.js")
       [:script {:type "text/javascript"}
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
