(ns jiksnu.rdf.sections.activity-sections
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
  (:import javax.xml.namespace.QName
           jiksnu.model.Activity
           jiksnu.model.Conversation
           jiksnu.model.Resource
           jiksnu.model.User
           org.apache.abdera.model.ExtensibleElement))

(defsection index-block [Activity :rdf]
  [items & [response & _]]
  (apply concat (map #(index-line % response) items)))

(defsection show-section [Activity :rdf]
  [activity & _]
  (plaza/with-rdf-ns ""
    (let [{:keys [id published content]} activity
          uri (full-uri activity)
          user (model.activity/get-author activity)
          user-res (plaza/rdf-resource (or #_(:id user) (model.user/get-uri user)))]
      (concat
       (rdf/with-subject uri
         (concat
          [
           [[ns/rdf  :type]        [ns/sioc "Post"]]
           [[ns/as   :verb]        (plaza/l "post")]
           [[ns/sioc :has_creator] user-res]
           [[ns/sioc :has_owner]   user-res]
           [[ns/as   :author]      user-res]
           ]
          (when-let [lit (-?> published .toDate plaza/date)]
            [
             [[ns/dc   :published]   lit]
             ])))
       (when content [[uri [ns/sioc  :content]    (plaza/l content)]])))))

