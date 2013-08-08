(ns jiksnu.modules.as.sections.user-sections
  (:use  [ciste.sections :only [defsection]]
         [ciste.sections.default :only [full-uri show-section]]
         [slingshot.slingshot :only [try+]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.model.user :as model.user]
            [lamina.trace :as trace])
  (:import jiksnu.model.User))

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

