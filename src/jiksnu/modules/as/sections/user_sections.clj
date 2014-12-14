(ns jiksnu.modules.as.sections.user-sections
  (:use  [ciste.sections :only [defsection]]
         [ciste.sections.default :only [full-uri show-section]]
         [slingshot.slingshot :only [try+]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.model.user :as model.user]
            [lamina.trace :as trace])
  (:import jiksnu.model.User))

;; show-section

(defsection show-section [User :json]
  [user & options]
  (let [{:keys [display-name id avatar-url]} user
        avatar-url (or avatar-url (model.user/image-link user))
        url (or (:url user)
                     #_(full-uri user)
                     (format "https://%s/api/user/%s/profile" (:domain user) (:username user))
                     )
        inbox-url (format "https://%s/api/user/%s/inbox" (:domain user) (:username user))
        outbox-url (format "https://%s/api/user/%s/outbox" (:domain user) (:username user))
        followers-url (format "https://%s/api/user/%s/followers" (:domain user) (:username user))
        following-url (format "https://%s/api/user/%s/following" (:domain user) (:username user))
        favorites-url (format "https://%s/api/user/%s/favorites" (:domain user) (:username user))
        list-url (format "https://%s/api/user/%s/lists/person" (:domain user) (:username user))
        ]
    (merge {:preferredUsername (:username user)
            :url url
            :displayName (:name user)
            :links {
                    :self {:href url}
                    :activity-inbox {:href inbox-url}
                    :activity-outbox {:href outbox-url}
                    }
            :objectType "person"
            :followers {
                        :url followers-url
                        :totalItems 0
                        }
            :following {
                        :url following-url
                        :totalItems 0
                        }
            :favorites {
                        :url favorites-url
                        :totalItems 0
                        }
            :lists {
                    :url list-url
                    :totalItems 0
                    }
            :image [{:url avatar-url
                     :rel "avatar"
                     :type "image/jpeg"
                     :width 96
                     :height 96
                     ;; :pump_io {
                     ;;           :proxyURL (proxy-url avatar-url)
                     ;;           }
                     }]

            :pump_io {
                      :fullImage {
                                  :url avatar-url
                                  :width 96
                                  :height 96
                                  }
                      :shared false
                      :followed false
                      }
            :summary ""
            :updated (:updated user)
            ;; :id (or id (model.user/get-uri user))


            :type "person"
            :id (:_id user)
            :username (:username user)
            :domain (:domain user)
            :published (:updated user)
            ;; :name {:formatted (:name user)
            ;;        :familyName (:last-name user)
            ;;        :givenName (:first-name user)}
            }
           (when display-name
             {:displayName display-name}))))

