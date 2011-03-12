(ns jiksnu.file
  (:use [clojure.java.io :only (reader resource)])
  (:require [clojure.xml :as xml]))

;; (defn classpath-file
;;   [file]
;;   (.getResourceAsStream
;;    (.getContextClassLoader
;;     (Thread/currentThread))
;;    file))

;; (defn read-xml
;;   [file]
;;   (let [cfile (classpath-file file)]
;;     (xml/parse cfile)))

;; (defn slurp-classpath
;;   [file]
;;   (with-open [rdr (reader (resource file))]
;;     (apply str (line-seq rdr))))

;; (defn read-clojure
;;   [filename]
;;   (read-string (slurp-classpath filename)))
