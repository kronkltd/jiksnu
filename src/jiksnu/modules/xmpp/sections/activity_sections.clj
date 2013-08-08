(ns jiksnu.modules.xmpp.sections.activity-sections
  (:use [ciste.core :only [with-format]]
        [ciste.sections :only [defsection]]
        [ciste.sections.default :only [index-section show-section index-block
                                       index-line]]
        [slingshot.slingshot :only [throw+]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.namespace :as ns]
            [jiksnu.modules.xmpp.element :as element])
  (:import jiksnu.model.Activity
           org.apache.abdera.model.Entry
           org.apache.abdera.model.ExtensibleElement))

(defn acl-link
  [^Entry entry activity]
  (if (:public activity)
    (let [^ExtensibleElement rule-element (.addExtension entry ns/osw "acl-rule" "")]
      (let [^ExtensibleElement action-element
            (.addSimpleExtension rule-element ns/osw
                                 "acl-action" "" ns/view)]
        (.setAttributeValue action-element "permission" ns/grant))
      (let [^ExtensibleElement subject-element
            (.addExtension rule-element ns/osw "acl-subject" "")]
        (.setAttributeValue subject-element "type" ns/everyone)))))

;; index-block

(defsection index-block [Activity :xmpp]
  [activities & options]
  ["items" {"node" ns/microblog}
   (map index-line activities)])

;; index-line

(defsection index-line [Activity :xmpp]
  [^Activity activity & options]
  ["item" {"id" (:_id activity)}
   (show-section activity)])

;; index-section

(defsection index-section [Activity :xmpp]
  [activities & options]
  ["pubsub" {} (index-block activities)])

;; show-section

(defsection show-section [Activity :xmpp]
  [^Activity activity & options]
  (element/abdera-to-tigase-element
   (with-format :atom
     (show-section activity))))
