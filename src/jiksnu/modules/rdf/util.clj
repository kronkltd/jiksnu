(ns jiksnu.modules.rdf.util
  (:require [jiksnu.namespace :as ns]
            [plaza.rdf.core :as plaza]
            [plaza.rdf.implementations.jena :as jena]))

;; TODO: The rdf backend should be configurable and this should be
;; initialized in the definitializer. I'm not certain that clj-plaza
;; will support this.
(jena/init-jena-framework)

(def rdf-prefixes
  [["activity" ns/as]
   ["sioc" ns/sioc]
   ["cert" ns/cert]
   ["foaf" ns/foaf]
   ["dc" ns/dc]
   ["xsd" (str ns/xsd "#")]])

(defn with-subject
  "Inserts the subject into the first position in the sequence of vectors"
  [s pairs]
  (map (fn [[p o]] [s p o]) pairs))

;; rdf helpers

(defn triples->model
  [triples]
  (let [model (plaza/build-model)
        j-model (plaza/to-java model)]
    (doseq [[prefix uri] rdf-prefixes]
      (.setNsPrefix j-model prefix uri))
    (plaza/with-model model
      (plaza/model-add-triples triples))
    model))

(defn format-triples
  [triples format]
  (-> triples
      triples->model
      (plaza/model->format format)
      with-out-str))

