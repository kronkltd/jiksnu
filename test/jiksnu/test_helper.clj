(ns jiksnu.test-helper
  (:require [ciste.runner :refer [start-application! stop-application!]]
            [hiccup.core :as h]
            [jiksnu.modules.core.db :as db]
            [jiksnu.referrant :as r]
            [midje.sweet :refer [=> =not=> fact future-fact namespace-state-changes throws]]
            [net.cgrand.enlive-html :as enlive]
            [slingshot.slingshot :refer [try+ throw+]]
            [taoensso.timbre :as timbre])
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

(def depth (ref 0))

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
  ([] (setup-testing nil))
  ([modules]
   (timbre/debugf "setup testing(%s) - %s" @depth modules)
   (try+

    (when (zero? @depth)
      (start-application! modules))

    (dosync
     (alter depth inc))

    ;; (loader/register-module "jiksnu.modules.core")
    ;; (db/drop-all! )
    (dosync
     (ref-set r/this {})
     (ref-set r/that {}))
    ;; (actions.domain/current-domain)
    (catch Object ex
      (timbre/error "Setup Error" ex)
      ;; FIXME: Handle error
      (throw+ ex)))))

(defn stop-testing
  []
  (try+
   (dosync
    (alter depth dec))

   (timbre/debugf "stop-testing(%s)" @depth)

   ;; (when (zero? @depth)
   ;;   (stop-application!))

   (catch Object ex
     ;(println "error")
     (timbre/error "Shutdown Error" ex)
     (throw+ ex))))

(defmacro module-test
  [modules]
  `(namespace-state-changes
    [(before :facts (setup-testing ~modules))
     (after :facts (stop-testing))]))

(defmacro test-environment-fixture
  [& body]
  `(try+
    (timbre/debug "wrapping testing fixture")
    (setup-testing)
    ;; (fact (do ~@body) =not=> (throws))
    ~@body
    (catch Object ex#
      (throw+ ex#))
    (finally
      (stop-application!))))
