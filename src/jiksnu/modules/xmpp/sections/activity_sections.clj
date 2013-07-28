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
            [jiksnu.abdera :as abdera]
            [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.actions.comment-actions :as actions.comment]
            [jiksnu.model :as model]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.conversation :as model.conversation]
            [jiksnu.model.like :as model.like]
            [jiksnu.model.resource :as model.resource]
            [jiksnu.model.user :as model.user]
            [jiksnu.namespace :as ns]
            [jiksnu.rdf :as rdf]
            [jiksnu.sections.user-sections :as sections.user]
            [jiksnu.session :as session]
            [jiksnu.util :as util]
            [jiksnu.xmpp.element :as element]
            [plaza.rdf.core :as plaza]
            [ring.util.codec :as codec])
  (:import java.io.StringWriter
           javax.xml.namespace.QName
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
