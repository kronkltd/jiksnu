(ns jiksnu.formats
  (:use [ciste.core :only [serialize-as with-format]]
        [ciste.config :only [config]]
        [ciste.formats :only [format-as]]
        [ciste.sections :only [defsection]]
        [ciste.sections.default :only [full-uri title link-to
                                       index-block index-section uri
                                       delete-button index-line edit-button]]
        [jiksnu.sections :only [format-page-info]])
  (:require [clj-tigase.core :as tigase]
            [clojure.data.json :as json]
            [clojure.tools.logging :as log]
            [hiccup.core :as h]
            [jiksnu.abdera :as abdera]
            [jiksnu.actions.subscription-actions :as actions.subscription]
            [jiksnu.model :as model]
            [jiksnu.namespace :as ns]
            [jiksnu.rdf :as rdf]
            jiksnu.sections
            [jiksnu.session :as session]
            [plaza.rdf.vocabularies.foaf :as foaf]))

(defmethod format-as :as
  [format request response]
  (with-format :json (format-as :json request response)))

(defmethod format-as :atom
  [format request response]
  (let [atom-map (-> (:body response)
                     (assoc :title (:title response)))]
    (-> response
        (assoc :body (abdera/make-feed atom-map)))))

(defmethod format-as :clj
  [format request response]
  (-> response
      (assoc-in  [:headers "Content-Type"] "text/plain")
      (assoc :body (str (:body response)))))

;; (defmethod format-as :default
;;   [format request response]
;;   response)

(defmethod format-as :html
  [format request response]
  (-> response
      (assoc :body (h/html (:body response)))))

(defmethod format-as :json
  [format request response]
  (-> response
      (assoc-in [:headers "Content-Type"] "application/json")
      (assoc :body (json/json-str (:body response)))))

(defmethod format-as :page
  [format request response]
  (with-format :json
    (doall (format-as :json request response))))

(defmethod format-as :model
  [format request response]
  (with-format :json
    (doall (format-as :json request response))))


(defmethod format-as :n3
  [request format response]
  (-> response
      (assoc :body (rdf/format-triples (:body response) :n3))
      (assoc-in [:headers "Content-Type"] "text/plain; charset=utf-8")))

(defmethod format-as :rdf
  [request format response]
  (-> response
      (assoc :body (rdf/format-triples (:body response) :xml-abbrev))
      (assoc-in [:headers "Content-Type"] "application/rdf+xml; charset=utf-8")))

(defmethod format-as :viewmodel
  [format request response]
  (let [response (if-let [id (session/current-user-id)]
                   (assoc-in response [:body :currentUser] id)
                   response)]
    (with-format :json
      (doall (format-as :json request response)))))

(defmethod format-as :xml
  [format request response]
  (-> response
      (assoc-in [:headers "Content-Type"] "application/xml")
      (assoc :body (h/html (:body response)))))

(defmethod format-as :xmpp
  [format request response]
  response)
