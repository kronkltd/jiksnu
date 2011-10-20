(ns jiksnu.templates.domain
  (:use (ciste [debug :only (spy)])
        closure.templates.core))

(defn format-data
  [domain]
  {:id (:_id domain)
   :xmpp (if-let [xmpp (:xmpp domain)] xmpp "Unknown")
   :discovered (:discovered domain)
   :link-count (Integer. (count (:links domain)))
   :links (map #(merge {:href "" :type ""
                        :rel "" :template ""} %)
               (:links domain))})

(deftemplate link-to
  [domain]
  (:id (:_id domain)))

(deftemplate show
  [domain]
  (format-data domain))

(deftemplate index-block
  [domains]
  {:domains (map format-data domains)})
