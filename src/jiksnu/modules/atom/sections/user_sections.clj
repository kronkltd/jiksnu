(ns jiksnu.modules.atom.sections.user-sections
  (:use [ciste.sections :only [defsection]]
        [ciste.sections.default :only [title full-uri show-section]]
        [slingshot.slingshot :only [try+]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.modules.atom.util :as abdera]
            [jiksnu.namespace :as ns]
            [jiksnu.model.user :as model.user]
            [lamina.trace :as trace])
  (:import jiksnu.model.User))

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

