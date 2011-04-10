(ns jiksnu.actions.webfinger-actions
  (:use ciste.debug
        jiksnu.namespace)
  (:require [jiksnu.model.user :as model.user])
  (:import com.cliqset.hostmeta.JavaNetXRDFetcher
           com.cliqset.xrd.XRD
           java.net.URL
           org.openxrd.xrd.core.impl.XRDBuilder))

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
     (let [href (str (.getHref link))
           rel (str (.getRel link))
           template (.getTemplate link)
           type (.getType link)]
       (merge
        {}
        (if href {:href href})
        (if rel {:rel rel})
        (if template {:template template})
        (if type {:type type}))))
   (.getLinks xrd)))
