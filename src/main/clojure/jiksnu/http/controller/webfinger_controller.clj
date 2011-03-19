(ns jiksnu.http.controller.webfinger-controller
  (:use jiksnu.namespace)
  (:require [jiksnu.model.user :as model.user])
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
  [{{uri "uri"} :params :as request}]
  (let [[_ username domain] (re-matches #"(?:acct:)(.*)@(.*)" uri)]
    (model.user/show username domain)))

(defn get-links
  [xrd]
  (map
   (fn [link]
     {:href (.getHref link)
      :rel (.getRel link)
      :template (.getTemplate link)
      :type (.getType link)

      })
   (.getLinks xrd))
  )
