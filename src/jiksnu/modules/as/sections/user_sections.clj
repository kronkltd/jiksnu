(ns jiksnu.sections.user-sections
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
            [hiccup.form :as f]
            [jiksnu.namespace :as ns]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.domain :as model.domain]
            [jiksnu.model.feed-source :as model.feed-source]
            [jiksnu.model.key :as model.key]
            [jiksnu.model.subscription :as model.subscription]
            [jiksnu.model.user :as model.user]
            [jiksnu.rdf :as rdf]
            [jiksnu.util :as util]
            [lamina.trace :as trace]
            [plaza.rdf.core :as plaza]
            [ring.util.codec :as codec])
  (:import jiksnu.model.Domain
           jiksnu.model.FeedSource
           jiksnu.model.Key
           jiksnu.model.User))

;; show-section

(defsection show-section [User :as]
  [user & options]
  (let [{:keys [display-name id avatar-url]} user
        avatar-url (or avatar-url (model.user/image-link user))]
    (merge {:preferredUsername (:username user)
            :url (or (:url user)
                     (full-uri user))
            :displayName (:name user)
            :links (:links user)
            :objectType "person"
            :followers {
                        :url "/followers"
                        :totalItems 0
                        }
            :updated (:updated user)
            :id (or id (model.user/get-uri user))


            :profileUrl (full-uri user)
            :type "person"
            :username (:username user)
            :domain (:domain user)
            :published (:updated user)
            ;; :name {:formatted (:name user)
            ;;        :familyName (:last-name user)
            ;;        :givenName (:first-name user)}
            }
           (when avatar-url
             ;; TODO: get image dimensions
             {:image [{:url avatar-url
                       :rel "avatar"}]})
           (when display-name
             {:displayName display-name}))))

