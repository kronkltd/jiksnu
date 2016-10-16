(ns jiksnu.modules.as.sections.user-sections
  (:require [ciste.config :refer [config]]
            [ciste.core :refer [with-context]]
            [ciste.sections :refer [defsection]]
            [ciste.sections.default :refer [show-section]]
            [jiksnu.model.domain :as model.domain]
            [jiksnu.model.user :as model.user])
  (:import jiksnu.model.User))

;; show-section

(def url-pattern       "%s://%s/main/users/%s")
(def profile-pattern   "%s://%s/api/user/%s/profile")
(def inbox-pattern     "%s://%s/api/user/%s/inbox")
(def outbox-pattern    "%s://%s/api/user/%s/feed")
(def followers-pattern "%s://%s/api/user/%s/followers")
(def following-pattern "%s://%s/api/user/%s/following")
(def favorites-pattern "%s://%s/api/user/%s/favorites")
(def lists-pattern     "%s://%s/api/user/%s/lists/person")

(defsection show-section [User :as]
  [user & options]
  (let [{:keys [display-name id avatar-url domain username]} user
        scheme (if (some-> domain model.domain/fetch-by-id :secure) "https" "http")
        avatar-url (or avatar-url (model.user/image-link user))
        url           (format url-pattern       scheme domain username)
        profile-url   (format profile-pattern   scheme domain username)
        inbox-url     (format inbox-pattern     scheme domain username)
        outbox-url    (format outbox-pattern    scheme domain username)
        followers-url (format followers-pattern scheme domain username)
        following-url (format following-pattern scheme domain username)
        favorites-url (format favorites-pattern scheme domain username)
        list-url      (format lists-pattern     scheme domain username)]
    (array-map
     :preferredUsername username
     :url url
     :displayName (:displayName user username)
     :links {:self            {:href profile-url}
             :activity-inbox  {:href inbox-url}
             :activity-outbox {:href outbox-url}}
     :objectType "person"
     :updated (:updated user)
     :published (:created user)
     :followers {:url followers-url :totalItems 0}
     :following {:url following-url :totalItems 0}
     :favorites {:url favorites-url :totalItems 0}
     :lists     {:url list-url      :totalItems 0}
     :image     {:url    (str avatar-url ".jpg")
                 :width  96
                 :height 96}
     :id (:_id user)
     ;; TODO: How are these determined?
     :liked false
     :summary "INSERT SUMMARY HERE"
     :pump_io {:shared false
               :followed false}
     :type "person")))

(defn format-collection
  [user page]
  (let [domain (config :domain)
        scheme (if (some-> domain model.domain/fetch-by-id :secure) "https" "http")]
    (with-context [:http :as]
      {:displayName (str "Collections of persons for " (:_id user))
       :objectTypes [(:objectTypes page "collection")]
       :url         (format lists-pattern scheme domain (:username user))
       :links       {:self {:href (format lists-pattern scheme domain (:username user))}}
       :items       (doall (map show-section (:items page)))
       :totalItems  (:totalItems page)
       :author      (show-section user)})))
