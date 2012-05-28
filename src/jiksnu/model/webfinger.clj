(ns jiksnu.model.webfinger
  (:use [ciste.config :only [config]]
        [ciste.debug :only [spy]]
        ciste.sections
        [ciste.sections.default :only [full-uri]]
        [clojure.core.incubator :only [-?>]])
  (:require [ciste.model :as cm]
            [clojure.tools.logging :as log]
            [jiksnu.model :as model]
            [jiksnu.namespace :as namespace]
            [jiksnu.model.key :as model.key]
            [jiksnu.model.user :as model.user])
  (:import java.net.URI))

(defn fetch-host-meta
  [url]
  (log/infof "fetching host meta: %s" url)
  (when-let [doc (cm/fetch-document url)]
    doc))

;; This function is a little too view-y. The proper representation of
;; a xrd document should be a hash with all this data.
(defn host-meta
  [domain]
  ["XRD" {"xmlns" namespace/xrd
          "xmlns:hm" namespace/host-meta}
   ["hm:Host" domain]
   ["Link" {"rel" "lrdd"
            "template" (str "http://" domain "/main/xrd?uri={uri}")}
    ["Title" {} "Resource Descriptor"]]])

(defn user-meta
  [lrdd]
  ["XRD" {"xmlns" namespace/xrd}
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

(defn get-links
  [xrd]
  (let [links (model/force-coll (cm/query "//*[local-name() = 'Link']" xrd))]
    (map
     (fn [link]
       {:rel (.getAttributeValue link "rel")
        :template (.getAttributeValue link "template")
        :href (.getAttributeValue link "href")
        :type (.getAttributeValue link "type")
        :lang (.getAttributeValue link "lang")})
     links)))

(defn get-identifiers
  "returns the values of the subject and it's aliases"
  [xrd]
  (->> (concat (model/force-coll (cm/query "//*[local-name() = 'Subject']" xrd))
               (model/force-coll (cm/query "//*[local-name() = 'Alias']" xrd)))
       (map #(.getValue %))))
