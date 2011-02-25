(ns jiksnu.http.controller.webfinger-controller
  (:use jiksnu.namespace)
  (:import org.openxrd.xrd.core.impl.XRDBuilder
           com.cliqset.xrd.XRD
           com.cliqset.hostmeta.JavaNetXRDFetcher
           java.net.URL))

(defonce #^:dynamic *fetcher*
  (JavaNetXRDFetcher.))

(defonce #^:dynamic *xrd-builder*
  (XRDBuilder.))

(defn fetch
  [url]
  (.fetchXRD *fetcher* (URL. url)))

(defn host-meta
  [request]
  (let [xrd (XRD.)]
    (let [host-element nil])
    (.buildObject *xrd-builder*)))

(defn user-meta
  [request]
  true)
