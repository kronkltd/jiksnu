(ns jiksnu.model.webfinger
  (:use [ciste.config :only [config]]
        [ciste.sections.default :only [full-uri]]
        [clojure.core.incubator :only [-?>]]
        [slingshot.slingshot :only [throw+]])
  (:require [ciste.model :as cm]
            [clj-statsd :as s]
            [clojure.tools.logging :as log]
            [jiksnu.model :as model]
            [jiksnu.namespace :as ns]
            [jiksnu.model.key :as model.key]
            [jiksnu.model.user :as model.user]
            [jiksnu.ops :as ops]
            [jiksnu.util :as util]
            [lamina.core :as l])
  (:import java.net.URI
           jiksnu.model.User
           nu.xom.Document))

(defn fetch-host-meta
  [url]
  {:pre [(string? url)]
   :post [(instance? Document %)]}
  (log/infof "fetching host meta: %s" url)
  (or
   (try
     (let [resource (ops/get-resource url)
           response (ops/update-resource resource)]
       (s/increment "xrd_fetched")
       (when (= 200 (:status response))
         (cm/string->document (:body response))))
     (catch RuntimeException ex))
   (throw+ "Could not fetch host meta")))

;; This function is a little too view-y. The proper representation of
;; a xrd document should be a hash with all this data.
(defn host-meta
  [domain]
  {:pre [(model/domain? domain)]
   :post [(vector? %)]}
  ["XRD" {"xmlns" ns/xrd
          "xmlns:hm" ns/host-meta}
   ["hm:Host" domain]
   ["Link" {"rel" "lrdd"
            "template" (str "http://" domain "/main/xrd?uri={uri}")}
    ["Title" {} "Resource Descriptor"]]])

(defn get-source-link
  [xrd]
  {:pre [(instance? Document xrd)]
   :post [(string? %)]}
  (let [query-str (format "//*[local-name() = 'Link'][@rel = '%s']" ns/updates-from)]
    (->> xrd
         (cm/query query-str)
         util/force-coll
         (keep #(.getAttributeValue % "href"))
         first)))


(defn get-feed-source-from-xrd
  [^Document xrd]
  (if-let [source-link (get-source-link xrd)]
    (ops/get-source source-link)
    (throw+ "could not determine source")))

(defn get-username-from-atom-property
  [^Document xrd]
  (try
    (->> xrd
         (cm/query "//*[local-name() = 'Property'][@type = 'http://apinamespace.org/atom/username']")
         util/force-coll
         (keep #(.getValue %))
         first)
    ;; TODO: What are the error risks here?
    (catch RuntimeException ex
      (log/error "caught error" ex)
      (.printStackTrace ex))))

(defn user-meta
  [lrdd]
  ["XRD" {"xmlns" ns/xrd}
   ["Subject" {} (:subject lrdd)]
   ["Alias" {} (:alias lrdd)]

   ;; Pull the links from a global ref that various plugins can write to
   (map
    (fn [link]
      ["Link"
       (merge
        (when (:rel link) {:rel (:rel link)})
        (when (:type link) {:type (:type link)})
        (when (:href link) {:href (:href link)}))
       (map
        (fn [property]
          ["Property"
           (merge
            (when (:type property) {:type (:type property)}))
           (:value property)])
        (:properties link))])
    (:links lrdd))])

(defn parse-link
  [link]
  (let [rel (.getAttributeValue link "rel")
        template (.getAttributeValue link "template")
        href (.getAttributeValue link "href")
        type (.getAttributeValue link "type")
        lang (.getAttributeValue link "lang")
        title (if-let [title-element (.getFirstChildElement link "Title" ns/xrd)]
                (.getValue title-element))]
    (merge (when rel      {:rel rel})
           (when template {:template template})
           (when href     {:href href})
           (when type     {:type type})
           (when title {:title title})
           (when lang     {:lang lang}))))

(defn get-links
  [xrd]
  (->> xrd
       (cm/query "//*[local-name() = 'Link']")
       util/force-coll
       (map parse-link)))

(defn get-identifiers
  "returns the values of the subject and it's aliases"
  [xrd]
  (->> (concat (util/force-coll (cm/query "//*[local-name() = 'Subject']" xrd))
               (util/force-coll (cm/query "//*[local-name() = 'Alias']" xrd)))
       (map #(.getValue %))))

(defn get-username-from-identifiers
  ;; passed a document
  [xrd]
  (try
    (->> xrd
         get-identifiers
         (keep (comp first util/split-uri))
         first)
    (catch RuntimeException ex
      (log/error "caught error" ex)
      (.printStackTrace ex))))

;; takes a document
(defn get-username-from-xrd
  "return the username component of the user meta"
  [xrd]
  (->> [(get-username-from-atom-property xrd)]
       (lazy-cat
        [(get-username-from-identifiers xrd)])
       (filter identity)
       first))

