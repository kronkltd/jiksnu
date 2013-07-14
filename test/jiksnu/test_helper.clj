(ns jiksnu.test-helper
  (:use [ciste.config :only [load-site-config]]
        [ciste.loader :only [process-requires]]
        [ciste.runner :only [start-application! stop-application!]]
        [midje.sweet :only [=> fact future-fact throws]]
        [slingshot.slingshot :only [try+ throw+]])
  (:require [clojure.tools.logging :as log]
            [hiccup.core :as h]
            [jiksnu.actions.domain-actions :as actions.domain]
            [jiksnu.db :as db]
            [jiksnu.model :as model]
            [jiksnu.referrant :as r]
            [lamina.trace :as trace]
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

(def ^:dynamic *depth* 0)

(defmacro context
  [description & body]
  `(let [var-name# (str ~description)]
     (print (apply str (repeat *depth* "  ")))
     (println var-name#)
     (fact var-name#

       ;; (trace/time*

        (binding [*depth* (inc *depth*)]
          ~@body)

        ;; )

        )
     (when (zero? *depth*)
       (println " "))))

(defmacro future-context
  [description & body]
  `(let [var-name# (str ~description)]
     (future-fact var-name# ~@body)))

(defmacro check
  [bindings & body]
  `(fn ~bindings
     (fact
       ~@body)))

(defmacro test-environment-fixture
  [& body]
  `(try
     (println " ")
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
     (finally
       (stop-application!))))
