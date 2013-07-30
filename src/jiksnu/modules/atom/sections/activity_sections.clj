(ns jiksnu.sections.activity-sections
  (:use [ciste.core :only [with-format]]
        [ciste.sections :only [defsection]]
        [ciste.sections.default :only [add-form delete-button edit-button
                                       full-uri index-section show-section-minimal
                                       show-section link-to uri title index-block
                                       index-line index-section update-button]]
        [clojure.core.incubator :only [-?>]]
        [jiksnu.ko :only [*dynamic*]]
        [jiksnu.sections :only [action-link actions-section admin-index-line admin-index-block
                                admin-index-section bind-property bind-to control-line
                                display-property display-timestamp
                                dropdown-menu dump-data format-links pagination-links]]
        [slingshot.slingshot :only [throw+]])
  (:require [ciste.model :as cm]
            [clojure.string :as string]
            [clojure.tools.logging :as log]
            [hiccup.core :as h]
            [jiksnu.modules.atom.util :as abdera]
            [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.actions.comment-actions :as actions.comment]
            [jiksnu.model :as model]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.conversation :as model.conversation]
            [jiksnu.model.like :as model.like]
            [jiksnu.model.resource :as model.resource]
            [jiksnu.model.user :as model.user]
            [jiksnu.namespace :as ns]
            [jiksnu.sections.user-sections :as sections.user]
            [jiksnu.session :as session]
            [jiksnu.util :as util]
            [jiksnu.xmpp.element :as element]
            [ring.util.codec :as codec])
  (:import javax.xml.namespace.QName
           jiksnu.model.Activity
           jiksnu.model.Conversation
           jiksnu.model.Resource
           jiksnu.model.User
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
        #_(actions.activity/comment-link-item activity)
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

