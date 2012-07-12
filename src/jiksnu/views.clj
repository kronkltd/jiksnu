(ns jiksnu.views
  (:use [ciste.core :only [serialize-as]]
        [ciste.config :only [config]]
        [ciste.formats :only [format-as]]
        [ciste.sections :only [defsection]]
        [ciste.sections.default :only [full-uri title link-to
                                       index-block index-section uri
                                       delete-button index-line edit-button]])
  (:require [clj-tigase.core :as tigase]
            [clojure.tools.logging :as log]
            [hiccup.core :as h]
            [jiksnu.model :as model]
            [jiksnu.namespace :as ns]
            jiksnu.sections
            [plaza.rdf.core :as rdf]
            [plaza.rdf.vocabularies.foaf :as foaf]))

(defmethod format-as :as
  [format request response]
  (format-as :json request response))

(defmethod format-as :html
  [format request response]
  (-> response
      (assoc :body (h/html (:body response)))))

(defmethod format-as :rdf
  [request format response]
  (-> response
      (assoc :body (model/format-triples (:body response) :xml-abbrev))
      (assoc-in [:headers "Content-Type"] "application/rdf+xml; charset=utf-8")))

(defmethod format-as :n3
  [request format response]
  (-> response 
      (assoc :body (model/format-triples (:body response) :n3))
      (assoc-in [:headers "Content-Type"] "text/plain; charset=utf-8")))

(defmethod format-as :xml
  [format request response]
  (-> response
      (assoc-in [:headers "Content-Type"] "application/xml")
      (assoc :body (h/html (:body response)))))




(defmethod serialize-as :http
  [serialization response-map]
  (let [content-type (or (-> response-map :headers (get "Content-Type"))
                         "text/html; charset=utf-8")]
    (-> (merge {:status 200} response-map)
        (assoc-in  [:headers "Content-Type"] content-type))))

(defmethod serialize-as :xmpp
  [serialization response]
  (when response
    (tigase/make-packet response)))
