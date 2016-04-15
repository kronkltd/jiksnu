(ns jiksnu.model.webfinger
  (:require [ciste.model :as cm]
            [jiksnu.namespace :as ns]
            [jiksnu.ops :as ops]
            [jiksnu.util :as util]
            [slingshot.slingshot :refer [throw+ try+]])
  (:import jiksnu.model.Domain
           jiksnu.model.FeedSource
           nu.xom.Document
           (nu.xom Element)))

(def mappings {"xrd" ns/xrd})

;; This function is a little too view-y. The proper representation of
;; a xrd document should be a hash with all this data.
(defn host-meta
  [^Domain domain]
  [:XRD {"xmlns" ns/xrd
          "xmlns:hm" ns/host-meta}
   [:hm:Host domain]
   [:Link {:rel "lrdd"
           :template (str "http://" domain "/main/xrd?uri={uri}")}
    [:Title {} "Resource Descriptor"]]])

(defn get-source-link
  "Returns a update link from a user meta"
  [^Document xrd]
  {:post [(or (nil? %) (string? %))]}
  (let [query-str (format "//xrd:Link[@rel = '%s']" ns/updates-from)]
    (-> xrd
         (cm/query query-str mappings)
         util/force-coll
         (->> (keep #(.getAttributeValue ^Element % "href")))
         first)))

(defn get-feed-source-from-xrd
  [^Document xrd]
  {:post [(instance? FeedSource %)]}
  (if-let [source-link (get-source-link xrd)]
    @(ops/get-source source-link)))

(defn get-username-from-atom-property
  [^Document xrd]
  {:post [(or (nil? %) (string? %))]}
  (let [query-str (str "//xrd:Property"
                       "[@type = 'http://apinamespace.org/atom/username']")]
    (let [root (.getRootElement xrd)]
      (->> (cm/query root query-str mappings)
           util/force-coll
           (keep (fn [^Element prop] (when prop (.getValue prop))))
           first))))

(defn user-meta
  [lrdd]
  [:XRD {:xmlns ns/xrd}
   [:Subject {} (:subject lrdd)]
   [:Alias {} (:alias lrdd)]
   ;; Pull the links from a global ref that various plugins can write to
   (map
    (fn [link]
      [:Link
       (merge
        (when (:rel link) {:rel (:rel link)})
        (when (:type link) {:type (:type link)})
        (when (:href link) {:href (:href link)}))
       (map
        (fn [property]
          [:Property
           (when-let [type (:type property)]
             {:type type})
           (:value property)])
        (:properties link))])
    (:links lrdd))])

(defn get-links
  [^Document xrd]
  (let [root (.getRootElement xrd)]
    (->> (cm/query root "//xrd:Link" mappings)
         util/force-coll
         (map util/parse-link))))

(defn get-identifiers
  "returns the values of the subject and it's aliases"
  [^Document xrd]
  {:post [(coll? %)]}
  (let [root (.getRootElement xrd)
        elts (concat (util/force-coll (cm/query root "//xrd:Subject" mappings))
                     (util/force-coll (cm/query root "//xrd:Alias" mappings)))]
    (map #(.getValue ^Element %) elts)))

(defn get-username-from-identifiers
  [^Document xrd]
  (try+
    (->> xrd
         get-identifiers
         (keep (comp first util/split-uri))
         first)
    (catch Throwable _
      ;; FIXME: Handle errors
      )))

;; takes a document
(defn get-username-from-xrd
  "return the username component of the user meta"
  [^Document xrd]
  (->> [(get-username-from-atom-property xrd)]
       (lazy-cat
        [(get-username-from-identifiers xrd)])
       (filter identity)
       first))
