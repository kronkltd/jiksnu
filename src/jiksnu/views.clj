(ns jiksnu.views
  (:use [ciste.core]
        [ciste.config :only [config]]
        [ciste.debug :only [spy]]
        ciste.formats
        [ciste.sections :only [defsection]]
        ciste.views
        ciste.sections.default
        ciste.views.default
        jiksnu.model
        jiksnu.session)
  (:require [clj-tigase.core :as tigase]
            [hiccup.core :as h]
            [jiksnu.namespace :as ns]
            [plaza.rdf.core :as rdf]
            [plaza.rdf.vocabularies.foaf :as foaf]))

(defn control-line
  [label name type & {:as options}]
  (let [{:keys [value checked]} options]
    [:div.control-group
     [:label.control-label {:for name} label]
     [:div.controls
      [:input
       (merge {:type type :name name}
              (when value
                {:value value})
              (when checked
                {:checked "checked"}))]]]))

(defsection full-uri :default
  [record & options]
  (str "http://" (config :domain)
       (apply uri record options)))

(defsection link-to :default
  [record & options]
  (let [options-map (apply hash-map options)]
    [:a {:href (uri record)}
     [:span {:about (uri record)
             :property "dc:title"}
      (or (:title options-map) (title record))] ]))

;; (defmethod apply-view-by-format :atom
;;   [request response])

(defn format-triples
  [triples format]
  (let [model (rdf/build-model)]
    (.setNsPrefix (rdf/to-java model) "activity" ns/as)
    (.setNsPrefix (rdf/to-java model) "sioc" ns/sioc)
    (.setNsPrefix (rdf/to-java model) "foaf" ns/foaf)
    (.setNsPrefix (rdf/to-java model) "dc" ns/dc)
    (.setNsPrefix (rdf/to-java model) "xsd" (str ns/xsd "#"))
    (.setNsPrefix (rdf/to-java model) "notice" (str "http://" (config :domain) "/notice/"))
    (.setNsPrefix (rdf/to-java model) "cert" ns/cert)
    
    (rdf/with-model model (rdf/model-add-triples triples))
    (with-out-str (rdf/model-to-format model format))))

(defmethod format-as :rdf
  [request format response]
  (-> response
      (assoc :body (format-triples (:body response) :xml-abbrev))
      (assoc-in [:headers "Content-Type"] "application/rdf+xml; charset=utf-8")))

(defmethod format-as :n3
  [request format response]
  (-> response 
      (assoc :body (format-triples (:body response) :n3))
      (assoc-in [:headers "Content-Type"] "text/plain; charset=utf-8")))

(defmethod format-as :xml
  [format request response]
  (-> response
      (assoc-in [:headers "Content-Type"] "application/xml")
      (assoc :body (h/html (:body response)))))

(defmethod format-as :as
  [format request response]
  (format-as :json request response))




(defmethod serialize-as :http
  [serialization response-map]
  (assoc-in
   (merge {:status 200} response-map)
   [:headers "Content-Type"]
   (or (-> response-map :headers (get "Content-Type"))
       "text/html; charset=utf-8")))

(defmethod serialize-as :xmpp
  [serialization response]
  (when response
    (tigase/make-packet response)))
