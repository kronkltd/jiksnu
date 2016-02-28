(ns jiksnu.modules.as.sections.user-sections
  (:require [ciste.config :refer [config]]
            [ciste.core :refer [with-context]]
            [ciste.sections :refer [defsection]]
            [ciste.sections.default :refer [full-uri show-section]]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.user :as model.user]
            [jiksnu.util :as util]
            [slingshot.slingshot :refer [try+]])
  (:import jiksnu.model.User))

;; show-section

(def url-pattern       "https://%s/%s")
(def profile-pattern   "https://%s/api/user/%s/profile")
(def inbox-pattern     "https://%s/api/user/%s/inbox")
(def outbox-pattern    "https://%s/api/user/%s/outbox")
(def followers-pattern "https://%s/api/user/%s/followers")
(def following-pattern "https://%s/api/user/%s/following")
(def favorites-pattern "https://%s/api/user/%s/favorites")
(def lists-pattern     "https://%s/api/user/%s/lists/person")

(defsection show-section [User :as]
  [user & options]
  (let [{:keys [display-name id avatar-url
                domain username]} user
        avatar-url (or avatar-url (model.user/image-link user))
        url           (format url-pattern domain username)
        profile-url   (format profile-pattern   domain username)
        inbox-url     (format inbox-pattern     domain username)
        outbox-url    (format outbox-pattern    domain username)
        followers-url (format followers-pattern domain username)
        following-url (format following-pattern domain username)
        favorites-url (format favorites-pattern domain username)
        list-url      (format lists-pattern     domain username)]
    (array-map
     :preferredUsername username
     :url url
     :displayName (:displayName user username)
     :links {:self            {:href profile-url}
             :activity-inbox  {:href inbox-url}
             :activity-outbox {:href outbox-url}}
     :objectType "person"
     :followers {:url followers-url
                 :totalItems 0}
     :following {:url following-url
                 :totalItems 0}
     :favorites {:url favorites-url
                 :totalItems 0}
     :lists {:url list-url
             :totalItems 0}
     :image {:url avatar-url
             ;; :rel "avatar"
             ;; :type "image/jpeg"
             :width 96
             :height 96
             }
     :updated (:updated user)
     :id (:_id user)
     ;; TODO: How are these determined?
     :liked false
     :pump_io {:shared false
               :followed false}
     :type "person")))

(defn format-collection
  [user page]
  (let [domain (config :domain)]
    (with-context [:http :as]
     {:displayName (str "Collections of persons for " (:_id user))
      :objectTypes [(:objectTypes page "collection")]
      :url (format lists-pattern domain (:username user))
      :links {
              :self {:href (format lists-pattern domain (:username user))}
              }
      :items (doall (map show-section (:items page)))
      :totalItems (:totalItems page)
      :author (show-section user)}))
  )
