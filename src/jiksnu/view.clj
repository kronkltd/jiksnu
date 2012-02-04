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
            (jiksnu.templates [layout :as templates.layout])
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

(defn page-template-content
  [response]
  {:headers {"Content-Type" "text/html; charset=utf-8"}
   :body
   (str
    "<!doctype html>\n"
    (h/html
     [:html
      {:xmlns:sioc ns/sioc}
      [:head
       [:meta {:charset "utf-8"}]
       [:title (when title (str title " - "))
        (config :site :name)]
       [:link {:type "text/css"
               :href "/bootstrap/bootstrap.css"
               :rel "stylesheet"
               :media "screen"}]
       [:link {:type "text/css"
               :href "/themes/classic/standard.css"
               :rel "stylesheet"
               :media "screen"}]]
      [:body
       [:header#site-header.topbar
        [:div.topbar-inner
         [:div.container
          [:a.brand.home {:href "/"} (config :site :name)]
          (navigation-section response)]]]
       [:div#wrap.container
        [:div#notification-area.row
         (when (:flash response)
           [:div#flash (:flash response)])]
        (devel-warning response)
        [:div#site-main.row
         [:div.span3
          (left-column-section response nil nil nil)]
         [:div#content.span13
          (:body response)]]]]]))})

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
      (assoc-in [:headers "Content-Type"] "text/plain")))



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
