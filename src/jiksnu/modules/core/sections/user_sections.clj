(ns jiksnu.modules.core.sections.user-sections
  (:require [ciste.config :refer [config]]
            [ciste.sections :refer [defsection]]
            [ciste.sections.default :refer [title uri show-section edit-button
                                            index-line show-section-minimal
                                            update-button index-block
                                            index-section]]
            [clojure.string :as string]
            [clojure.tools.logging :as log]
            [hiccup.core :as h]
            [inflections.core :as inf]
            [jiksnu.actions.subscription-actions :as actions.subscription]
            [jiksnu.model.user :as model.user]
            [jiksnu.namespace :as ns]
            [jiksnu.modules.core.sections :refer [admin-index-block
                                                  admin-index-line
                                                  admin-index-section
                                                  admin-show-section]]
            [slingshot.slingshot :refer [try+]])
  (:import jiksnu.model.Domain
           jiksnu.model.FeedSource
           jiksnu.model.Key
           jiksnu.model.User))

(defn user-timeline-link
  [user format]
  (str "http://" (config :domain)
       "/api/statuses/user_timeline/" (:_id user) "." format))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Sections
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defsection index-line [User :model]
  [item & page]
  (show-section item page))

(defsection show-section [User :model]
  [user & _]
  user)

(defsection show-section [User :xml]
  [user & options]
  [:user
   [:id (:_id user)]
   [:name (:name user)]
   [:screen_name (:username user)]
   [:location (:location user)]
   [:description (:bio user)]
   [:profile_image_url (h/h (:avatarUrl user))]
   [:url (:url user)]
   [:protected "false"]])

;; title

(defsection title [User]
  [user & options]
  (or (:name user)
      (:first-name user)
      (model.user/get-uri user)))

;; uri

(defsection uri [User]
  [user & options]
  (if (model.user/local? user)
    (str "/" (:username user))
    (str "/remote-user/" (:username user) "@" (:domain user))))
