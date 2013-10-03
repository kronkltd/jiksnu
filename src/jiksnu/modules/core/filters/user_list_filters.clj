(ns jiksnu.modules.web.filters.user-list-filters
  (:require [ciste.filters :refer [deffilter]]
            [jiksnu.actions.favorite-actions :as actions.favorite]
            [jiksnu.model.user :as model.user]))

(defview #'actions.user-list/user-list :as
  [request [user {:keys [items] :as response}]]
  {:template false
   :body {:items (index-section items response)}})

