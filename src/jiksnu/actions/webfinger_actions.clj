(ns jiksnu.actions.webfinger-actions
  (:use (ciste [core :only (defaction)]
               [debug :only (spy)])
        (clojure.contrib [core :only (-?>)])
        (jiksnu model))
  (:require (clojure.tools [logging :as log])
            (jiksnu [namespace :as namespace])
            (jiksnu.actions [domain-actions :as actions.domain]
                            [user-actions :as actions.user])
            (jiksnu.helpers [user-helpers :as helpers.user])
            (jiksnu.model [domain :as model.domain]
                          [signature :as model.signature]
                          [user :as model.user]))
  (:import com.cliqset.hostmeta.JavaNetXRDFetcher
           com.cliqset.hostmeta.HostMeta
           com.cliqset.hostmeta.HostMetaException
           com.cliqset.magicsig.keyfinder.MagicPKIKeyFinder
           com.cliqset.xrd.XRD
           java.net.URI
           java.net.URL
           jiksnu.model.Domain
           jiksnu.model.User
           org.openxrd.xrd.core.impl.XRDBuilder))

(defonce ^:dynamic *fetcher*
  (JavaNetXRDFetcher.))

(defonce ^:dynamic *xrd-builder*
  (XRDBuilder.))

;; TODO: rename fetch-host-meta
(defn fetch-host-meta
  "returns a cliqset xrd corresponding to the given url"
  [url]
  (if url
    (try
      (.fetchXRD *fetcher* (URL. url))
      (catch Exception e))))

(defaction host-meta
  []
  (let [xrd (XRD.)]
    ;; TODO: add the other info items
    (.buildObject *xrd-builder*)))

(defaction user-meta
  [uri]
  (->> uri
       model.user/split-uri
       (apply model.user/show )))

(defn parse-link
  "Turns a XRD link into a may with the same info"
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

(defn get-user-meta-uri
  [user]
  (let [username (:username user)
        domain (model.user/get-domain user)]
    (or (:user-meta-uri user)
        (actions.domain/get-user-meta-uri domain username))))

(defn fetch-user-meta
  [^User user]
  (-> user
      model.user/user-meta-uri
      fetch-host-meta))



(defn get-links
  [^XRD xrd]
  (if xrd (map parse-link (.getLinks xrd))))

(defn get-keys-from-xrd
  [uri]
  (let [host-meta (HostMeta/getDefault)
        key-finder (MagicPKIKeyFinder. host-meta)]
    (seq (.findKeys key-finder (URI. uri)))))

;; TODO: Collect all changes and update the user once.
(defaction update-usermeta
  [user]
  (let [xrd (fetch-user-meta (spy user))
        links (get-links (spy xrd))
        new-user (assoc user :links (spy links))
        feed (helpers.user/fetch-user-feed new-user)
        uri (if feed (-?> feed
                          .getAuthor
                          .getUri))]
    (doseq [link links]
      ;; TODO: process this in a trigger
      (if (= (:rel link) "magic-public-key")
        (let [key-string (:href link)
              [_ n e]
              (re-matches
               #"data:application/magic-public-key,RSA.(.+)\.(.+)"
               key-string)]
          (model.signature/set-armored-key (:_id user) n e)))
      (actions.user/add-link user link))
    (-> user
        (assoc :id (str uri))
        (assoc :discovered true)
        actions.user/update)))

(defn discover-webfinger
  [^Domain domain]
  ;; TODO: check https first
  (try
    (let [url (str "http://" (:_id domain) "/.well-known/host-meta")]
      (if-let [xrd (fetch-host-meta url)]
        (if-let [links (get-links xrd)]
          ;; TODO: These should call actions
          (do (model.domain/add-links domain links)
              (model.domain/set-discovered domain))
          (log/error "Host meta does not have any links"))
        (log/error
         (str "Could not find host meta for domain: " (:_id domain)))))
    (catch HostMetaException e
      (log/error e))))

