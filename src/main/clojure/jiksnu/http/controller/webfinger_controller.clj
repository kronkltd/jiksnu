(ns jiksnu.http.controller.webfinger-controller
  (:import org.openxrd.xrd.core.impl.XRDBuilder
           com.cliqset.xrd.XRD
           com.cliqset.hostmeta.JavaNetXRDFetcher
           java.net.URL)
  )

(defonce #^:dynamic *fetcher*
  (JavaNetXRDFetcher.)
  )

(defonce #^:dynamic *xrd-builder*
  (XRDBuilder.)
  )

(defn fetch
  [url]
  (.fetchXRD *fetcher* (URL. url))
  )

(def xrd-ns "http://docs.oasis-open.org/ns/xri/xrd-1.0")
(def host-meta-ns "http://host-meta.net/xrd/1.0")

(defn host-meta
  [request]
  (let [xrd (XRD.)]
    (let [host-element nil])
    (.buildObject *xrd-builder*)))

(defn user-meta
  [request]
  true
  )
