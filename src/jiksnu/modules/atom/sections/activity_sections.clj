(ns jiksnu.modules.atom.sections.activity-sections
  (:use [ciste.core :only [with-format]]
        [ciste.sections :only [defsection]]
        [ciste.sections.default :only [full-uri show-section]]
        [slingshot.slingshot :only [throw+]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.modules.atom.util :as abdera]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.namespace :as ns])
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

;; show-section

(defsection show-section [Activity :atom]
  [^Activity activity & _]
  (if-let [user (model.activity/get-author activity)]
    (let [entry (abdera/new-entry)]
      (when-let [published (:published activity)]
        (.setPublished entry (.toDate published)))
      (when-let [updated (:updated activity)]
        (.setUpdated entry (.toDate updated)))
      (doto entry
        (.setId (or (:id activity) (str (:_id activity))))
        (.setTitle (or (and (not= (:title activity) "")
                            (:title activity))
                       (:content activity)))
        (.addAuthor (show-section user))
        (.addLink (full-uri activity) "alternate")
        (.setContentAsHtml (:content activity))
        (.addSimpleExtension ns/as "object-type" "activity" ns/status)
        (.addSimpleExtension ns/as "verb" "activity" ns/post)
        (acl-link activity))
      (let [object (:object activity)
            object-element (.addExtension entry ns/as "object" "activity")]
        #_(.setObjectType object-element ns/status)
        (if-let [object-updated (:updated object)]
          (.addSimpleExtension object-element ns/atom "updated" "" (str object-updated)))
        (if-let [object-published (:published object)]
          (.addSimpleExtension object-element ns/atom "published" "" (str object-published)))
        #_(if-let [object-id (:id object)]
            (.setId object-element object-id))
        #_(.setContentAsHtml object-element (:content activity)))
      entry)
    (throw+ "Could not determine author")))

