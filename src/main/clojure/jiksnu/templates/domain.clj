(ns jiksnu.templates.domain
  (:use ciste.debug
        closure.templates.core))

(defn format-data
  [domain]
  {:id (:_id domain)
   :xmpp (if-let [xmpp (:xmpp domain)] xmpp "Unknown")
   :discovered (:discovered domain)
   :link-count (Integer. 0)
   :links []})

(deftemplate link-to
  [domain]
  (:id (:_id domain)))

(deftemplate show
  [domain]
  (format-data domain))

(deftemplate index-block
  [domains]
  {:domains (map format-data domains)})
