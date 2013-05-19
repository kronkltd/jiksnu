(ns jiksnu.test-helper
  (:use [ciste.config :only [load-site-config]]
        [ciste.loader :only [process-requires]]
        [ciste.runner :only [start-application! stop-application!]]
        midje.sweet
        [slingshot.slingshot :only [try+ throw+]])
  (:require [clojure.tools.logging :as log]
            [hiccup.core :as h]
            [jiksnu.actions.domain-actions :as actions.domain]
            [jiksnu.db :as db]
            [jiksnu.model :as model]
            [jiksnu.referrant :as r]
            [net.cgrand.enlive-html :as enlive])
  (:import java.io.StringReader))

(defn hiccup->doc
  [hiccup-seq]
  (-> hiccup-seq
      h/html
      StringReader.
      enlive/xml-resource))

(defn select-by-model
  [doc model-name]
  (->> [(enlive/attr= :data-model model-name)]
       (enlive/select doc)))

(defmacro test-environment-fixture
  [& body]
  `(do
     (println "****************************************************************************")
     (println (str "Testing " *ns*))
     (println "****************************************************************************")
     (println " ")
     (load-site-config)
     (start-application! :test)

     (db/drop-all!)

     (dosync
      (ref-set r/this {})
      (ref-set r/that {}))

     (actions.domain/current-domain)

     (fact (do ~@body) =not=> (throws))
     (stop-application!)))
