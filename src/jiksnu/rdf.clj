(ns jiksnu.rdf)

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

(jena/init-jena-framework)

;; rdf helpers

(defn triples->model
  [triples]
  (let [model (rdf/build-model)
        j-model (rdf/to-java model)]
    (doseq [[prefix uri] rdf-prefixes]
      (.setNsPrefix j-model prefix uri))
    (rdf/with-model model
      (rdf/model-add-triples triples))
    model))

(defn format-triples
  [triples format]
  (-> triples
      triples->model
      (rdf/model->format format)
      with-out-str))

