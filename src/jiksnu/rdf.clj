(ns jiksnu.rdf
  (:use [clojure.core.incubator :only [-?> -?>>]]
        [slingshot.slingshot :only [throw+]])
  (:require [ciste.model :as cm]
            [clj-statsd :as s]
            [clojure.string :as string]
            [clojure.data.json :as json]
            [clojure.tools.logging :as log]
            [inflections.core :as inf]
            [jiksnu.db :as db]
            [jiksnu.namespace :as ns]
            [jiksnu.util :as util]
            [lamina.core :as l]
            [lamina.time :as time]
            [lamina.trace :as trace]
            [plaza.rdf.core :as rdf]
            [plaza.rdf.implementations.jena :as jena])
  (:import com.mongodb.WriteConcern
           com.ocpsoft.pretty.time.PrettyTime
           java.io.FileNotFoundException
           java.io.PrintWriter
           java.text.SimpleDateFormat
           java.util.Date
           java.net.URL
           lamina.core.channel.Channel
           org.bson.types.ObjectId
           org.joda.time.DateTime
           java.io.StringReader))

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

