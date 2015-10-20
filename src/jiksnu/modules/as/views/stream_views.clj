(ns jiksnu.modules.as.views.stream-views
  (:require [ciste.views :refer [defview]]
            [ciste.sections.default :refer [show-section index-section]]
            [taoensso.timbre :as log]
            [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.actions.stream-actions :as actions.stream]))

;; (defview #'actions.stream/inbox :as
;;   [request [user page]]
;;   (let [inbox-major-url (format "https://%s/api/user/%s/inbox/major"
;;                                 (:domain user) (:username user))]
;;     {:body {:displayName (format "Activities for %s" (or
;;                                                       (:name user)
;;                                                       (:_id user)))
;;             :author (show-section user)
;;             :url inbox-major-url
;;             :links {
;;                     :first {
;;                             :href inbox-major-url
;;                             }
;;                     :self {
;;                            :href inbox-major-url
;;                            }
;;                     }
;;             :items (doall (index-section (:items page) page))
;;             :totalItems (:totalItems page)
;;             }}))

(defview #'actions.stream/inbox-major :as
  [request [user page]]
  (let [inbox-major-url (format "https://%s/api/user/%s/inbox/major"
                                (:domain user) (:username user))]
    {:body {:displayName (format "Major activities for %s" (or
                                                            (:name user)
                                                            (:_id user)))
            :author (show-section user)
            :url inbox-major-url
            :links {
                    :first {
                            :href inbox-major-url
                            }
                    :self {
                           :href inbox-major-url
                           }
                    }
            :items (doall (index-section (:items page) page))
            :totalItems (:totalItems page)
            }}))

(defview #'actions.stream/inbox-minor :as
  [request [user page]]
  {:body {:title (str (:name user) " Timeline")
          :author (show-section user)
          :items (index-section (:items page) page)}})

(defview #'actions.stream/direct-inbox-major :as
  [request [user page]]
  {:body {:title (str (:name user) " Timeline")
          :items (index-section (:items page) page)}})

(defview #'actions.stream/direct-inbox-minor :as
  [request [user page]]
  {:body {:title (str (:name user) " Timeline")
          :items (index-section (:items page) page)}})

(defview #'actions.stream/public-timeline :as
  [request {:keys [items] :as page}]
  {:body
   ;; TODO: I know that doesn't actually work.
   ;; TODO: assign the generator in the formatter
   {:generator "Jiksnu ${VERSION}"
    :title "Public Timeline"
    :totalItems (:totalItems page)
    :items
    (let [activity-page (actions.activity/fetch-by-conversations
                         (map :_id items))]
      (index-section (:items activity-page) activity-page))}})

(defview #'actions.stream/user-timeline :as
  [request [user {:keys [items] :as page}]]
  {:body
   {:title (str (:name user) " Timeline")
    :items
    (index-section items page)}})
