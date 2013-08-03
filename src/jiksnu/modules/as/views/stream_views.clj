(ns jiksnu.modules.as.views.stream-views
  (:use [ciste.views :only [defview]]
        [ciste.sections.default :only [index-section]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.actions.stream-actions :as actions.stream]))

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

