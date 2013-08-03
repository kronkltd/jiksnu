(ns jiksnu.modules.core.sections.user-sections
  (:use  [ciste.config :only [config]]
         [ciste.sections :only [defsection]]
         [ciste.sections.default :only [title uri show-section edit-button index-line
                                        show-section-minimal update-button index-block
                                        index-section]]
         [clojure.core.incubator :only [-?>]]
         [inflections.core :only [camelize]]
         [jiksnu.ko :only [*dynamic*]]
         [jiksnu.modules.core.sections :only [admin-index-block admin-index-line admin-index-section
                                              admin-show-section]]
         [jiksnu.modules.web.sections :only [display-property dropdown-menu pagination-links]]
         [jiksnu.session :only [current-user is-admin?]]
         [slingshot.slingshot :only [try+]])
  (:require [clojure.string :as string]
            [clojure.tools.logging :as log]
            [hiccup.core :as h]
            [hiccup.form :as f]
            [jiksnu.namespace :as ns]
            [jiksnu.actions.subscription-actions :as actions.subscription]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.domain :as model.domain]
            [jiksnu.model.feed-source :as model.feed-source]
            [jiksnu.model.key :as model.key]
            [jiksnu.model.subscription :as model.subscription]
            [jiksnu.model.user :as model.user]
            [lamina.trace :as trace]
            [ring.util.codec :as codec])
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

(defsection admin-index-block [User :viewmodel]
  [items & [page]]
  (->> items
       (map (fn [m] (index-line m page)))
       doall))

(defsection admin-index-section [User :viewmodel]
  [items & [page]]
  (admin-index-block items page))

(defsection index-block [User :viewmodel]
  [items & [page]]
  (->> items
       (map (fn [m] (index-line m page)))
       doall))

(defsection index-line [User :model]
  [item & page]
  (show-section item page))

(defsection index-line [User :viewmodel]
  [item & page]
  (show-section item page))

(defsection index-section [User :viewmodel]
  [items & [page]]
  (index-block items page))

(defsection show-section [User :json]
  [user & _]
  {:name (:name user)
   :id (:_id user)
   :screen_name (:username user)
   :url (:id user)
   :profile_image_url (:avatarUrl user)
   :protected false})

(defsection show-section [User :model]
  [user & _]
  user)

(defsection show-section [User :model]
  [item & [page]]
  (->>
   ;; (dissoc item :links)
   item

       (map (fn [[k v]] [(camelize (name k) :lower)
                         v]))
       (into {})))

(defsection show-section [User :viewmodel]
  [item & [page]]
  (->> #_(dissoc (dissoc item :links) :_id)
       item
       (map (fn [[k v]] [(camelize (name k) :lower)
                         v]))
       (into {})))

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
  (when-not *dynamic*
    (if (model.user/local? user)
      (str "/" (:username user))
      (str "/remote-user/" (:username user) "@" (:domain user)))))
