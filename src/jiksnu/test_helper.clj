(ns jiksnu.test-helper
  (:require [ciste.config :refer [load-site-config set-environment!]]
            [ciste.runner :refer [start-application! stop-application!]]
            [clojure.tools.logging :as log]
            [hiccup.core :as h]
            [jiksnu.actions.domain-actions :as actions.domain]
            [jiksnu.db :as db]
            [jiksnu.model :as model]
            [jiksnu.referrant :as r]
            [midje.sweet :refer [=> =not=> fact future-fact throws]]
            [net.cgrand.enlive-html :as enlive]
            [slingshot.slingshot :refer [try+ throw+]])
  (:import java.io.StringReader))

(defn hiccup->doc
  [hiccup-seq]
  (-> hiccup-seq
      h/html
      StringReader.
      enlive/xml-resource))

(defn select-by-model
  [doc model-name]
  (enlive/select doc [(enlive/attr= :data-model model-name)]))

(def ^:dynamic *depth* 0)

(defmacro context
  [description & body]
  `(let [var-name# (str ~description)]
     (print (apply str (repeat *depth* "  ")))
     (println var-name#)
     (fact ~description
       (binding [*depth* (inc *depth*)]
         ~@body))
     (when (zero? *depth*)
       (println " "))))

(defmacro future-context
  [description & body]
  `(let [var-name# (str ~description)]
     (future-fact var-name# ~@body)))

(defn setup-testing
  []
  (try+
   (load-site-config)
   (set-environment! :test)
   (start-application! :test)
   (db/drop-all! )
   (dosync
    (ref-set r/this {})
    (ref-set r/that {}))
   (actions.domain/current-domain)
   (catch Object ex
     ;; FIXME: Handle error
     (throw+ ex))))

(defn stop-testing
  []
  (try+
   (stop-application!)
   (catch Object ex
     (println "error")
     (log/error ex)
     #_(throw+ ex#))))

(defmacro test-environment-fixture
  [& body]
  `(try+
    (setup-testing)
    ;; (fact (do ~@body) =not=> (throws))
    ~@body
    (catch Object ex#
      (throw+ ex#))
    (finally
      (stop-application!))))
