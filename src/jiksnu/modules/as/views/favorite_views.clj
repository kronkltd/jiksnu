(ns jiksnu.modules.as.views.favorite-views
  (:require [ciste.views :refer [defview]]
            [ciste.sections.default :refer [index-section]]
            [jiksnu.actions.favorite-actions :as actions.favorite]))

(defview #'actions.favorite/user-list :as
  [request [user page]]
  {:body {:title (str (:name user) " Favorites")
          :items (index-section (:items page) page)}})
