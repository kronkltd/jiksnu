(ns jiksnu.modules.atom.sections.user-sections
  (:use  [ciste.config :only [config]]
         [ciste.sections :only [defsection]]
         [ciste.sections.default :only [title uri full-uri show-section add-form
                                        edit-button delete-button link-to index-line
                                        show-section-minimal update-button index-block
                                        index-section]]
         [clojure.core.incubator :only [-?>]]
         [inflections.core :only [camelize]]
         [jiksnu.ko :only [*dynamic*]]
         [jiksnu.sections :only [action-link actions-section admin-actions-section
                                 admin-index-block admin-index-line admin-index-section
                                 admin-show-section bind-property bind-to control-line
                                 display-property dropdown-menu pagination-links]]
         [jiksnu.session :only [current-user is-admin?]]
         [slingshot.slingshot :only [try+]])
  (:require [clojure.string :as string]
            [clojure.tools.logging :as log]
            [hiccup.core :as h]
            [hiccup.form :as f]
            [jiksnu.modules.atom.util :as abdera]
            [jiksnu.namespace :as ns]
            [jiksnu.actions.subscription-actions :as actions.subscription]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.domain :as model.domain]
            [jiksnu.model.feed-source :as model.feed-source]
            [jiksnu.model.key :as model.key]
            [jiksnu.model.subscription :as model.subscription]
            [jiksnu.model.user :as model.user]
            [jiksnu.util :as util]
            [lamina.trace :as trace]
            [ring.util.codec :as codec])
  (:import jiksnu.model.Domain
           jiksnu.model.FeedSource
           jiksnu.model.Key
           jiksnu.model.User))

(defn user->person
  [user]
  (let [author-uri (or (:url user)
                       (model.user/get-uri user))
        name (or (:name user)
                 (str (:first-name user) " " (:last-name user)))
        id (or (:id user) author-uri)
        extensions [{:ns ns/as
                     :local "object-type"
                     :prefix "activity"
                     :element ns/person}]
        params {:name name
                :extension extensions}
        person (abdera/make-person params)]
    (doto person

      (.addSimpleExtension ns/as   "object-type"       "activity" ns/person)
      (.addSimpleExtension ns/atom "id"                ""         id)
      (.addSimpleExtension ns/atom "uri"               ""         author-uri)
      (.addSimpleExtension ns/poco "preferredUsername" "poco"     (:username user))
      (.addSimpleExtension ns/poco "displayName"       "poco"     (title user))

      (.addExtension (doto (.newLink abdera/abdera-factory)
                       (.setHref (:avatarUrl user))
                       (.setRel "avatar")
                       (.setMimeType "image/jpeg")))
      (.addExtension (doto (.newLink abdera/abdera-factory)
                       (.setHref author-uri)
                       (.setRel "alternate")
                       (.setMimeType "text/html")))

      (-> (.addExtension ns/status "profile_info" "statusnet")
          (.setAttributeValue "local_id" (str (:_id user))))

      (-> (.addExtension ns/poco "urls" "poco")
          (doto (.addSimpleExtension ns/poco "type" "poco" "homepage")
            (.addSimpleExtension ns/poco "value" "poco" (full-uri user))
            (.addSimpleExtension ns/poco "primary" "poco" "true"))))

    (when (:email user)
      (.addSimpleExtension person ns/atom "email" "" (:email user)))

    person))

(defsection show-section [User :atom]
  [user & _]
  (user->person user))

