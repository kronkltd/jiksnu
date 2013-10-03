(ns jiksnu.modules.as.views.stream-views
  (:require [ciste.views :refer [defview]]
            [ciste.sections.default :refer [index-section]]
            [clojure.tools.logging :as log]
            [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.actions.stream-actions :as actions.stream]))

(defview #'actions.stream/inbox-major :as
  [request [user page]]
  {:body {:title (str (:name user) " Timeline")
          :items (index-section (:items page) page)}})

(defview #'actions.stream/inbox-minor :as
  [request [user page]]
  {:body {:title (str (:name user) " Timeline")
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
    :totalItems (:totalRecords page)
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

