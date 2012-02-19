(ns jiksnu.views
  (:use (ciste core
               [config :only [config]]
               [debug :only [spy]]
               formats sections views)
        ciste.sections.default
        (jiksnu model session))
  (:require (clj-tigase [core :as tigase])
            (hiccup [core :as h])
            (jiksnu [namespace :as ns]
                    [xmpp :as xmpp])
            (plaza.rdf [core :as rdf])
            (plaza.rdf.vocabularies [foaf :as foaf])))

(defn control-line
  [label name type & options]
  [:div.control-group
   [:label.control-label {:for name} label]
   [:div.controls
    [:input {:type type :name name}]]])

(defsection full-uri :default
  [record & options]
  (str "http://" (config :domain)
       (apply uri record options)))

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

;; (defmethod apply-view-by-format :atom
;;   [request response])




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
