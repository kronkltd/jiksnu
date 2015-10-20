(ns jiksnu.modules.as.views.user-list-views
  (:require [ciste.sections.default :refer [index-section]]
            [ciste.views :refer [defview]]
            [taoensso.timbre :as log]
            [jiksnu.actions.user-list-actions :as actions.user-list]
            )
  )

(defview #'actions.user-list/user-list :as
  [request [user {:keys [items] :as page}]]
  {:body
   {:title (str (:name user) " User Lists")
    :items
    (index-section items page)}})
