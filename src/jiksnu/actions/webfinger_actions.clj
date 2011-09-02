(ns jiksnu.actions.webfinger-actions
  (:use (ciste core debug)
        (jiksnu model
                namespace))
  (:require (jiksnu.model [user :as model.user]))
  (:import com.cliqset.hostmeta.JavaNetXRDFetcher
           com.cliqset.hostmeta.HostMeta
           com.cliqset.magicsig.keyfinder.MagicPKIKeyFinder
           java.net.URI
           com.cliqset.xrd.XRD
           java.net.URL
           org.openxrd.xrd.core.impl.XRDBuilder))

(defonce ^:dynamic *fetcher*
  (JavaNetXRDFetcher.))

(defonce ^:dynamic *xrd-builder*
  (XRDBuilder.))

(defn fetch
  [url]
  (if url (.fetchXRD *fetcher* (URL. url))))

(defaction host-meta
  []
  (let [xrd (XRD.)]
    ;; TODO: add the other info items
    (.buildObject *xrd-builder*)))

(defaction user-meta
  [uri]
  (let [[_ username domain] (re-matches #"(?:acct:)(.*)@(.*)" uri)]
    (model.user/show username domain)))

(defn parse-link
  [link]
  (let [href (str (.getHref link))
        rel (str (.getRel link))
        template (.getTemplate link)
        type (.getType link)]
    (merge {}
           (if href {:href href})
           (if rel {:rel rel})
           (if template {:template template})
           (if type {:type type}))))

(defn get-links
  [^XRD xrd]
  (map parse-link (.getLinks xrd)))

(defn get-keys
  [uri]
  (let [host-meta (HostMeta/getDefault)
        key-finder (MagicPKIKeyFinder. host-meta)]
    (seq (.findKeys key-finder (URI. (spy uri))))))
