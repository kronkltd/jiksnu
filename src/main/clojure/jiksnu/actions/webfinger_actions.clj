(ns jiksnu.actions.webfinger-actions
  (:use ciste.core
        ciste.debug
        jiksnu.model
        jiksnu.namespace)
  (:require [jiksnu.model.user :as model.user])
  (:import com.cliqset.hostmeta.JavaNetXRDFetcher
           com.cliqset.hostmeta.HostMeta
           com.cliqset.magicsig.keyfinder.MagicPKIKeyFinder
           java.net.URI
           com.cliqset.xrd.XRD
           java.net.URL
           org.openxrd.xrd.core.impl.XRDBuilder))

(defonce #^:dynamic *fetcher*
  (JavaNetXRDFetcher.))

(defonce #^:dynamic *xrd-builder*
  (XRDBuilder.))

(defn fetch
  [url]
  (if url
    (.fetchXRD *fetcher* (URL. url))))

(defaction host-meta
  []
  (let [xrd (XRD.)]
    (let [host-element nil])
    (.buildObject *xrd-builder*)))

(defaction user-meta
  [uri]
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

(defn get-keys
  [uri]
  (let [host-meta (HostMeta/getDefault)
        key-finder (MagicPKIKeyFinder. host-meta)]
    (.findKeys key-finder (URI. uri))))
