(ns jiksnu.modules.as.views.user-views
  (:require [ciste.sections.default :refer [show-section]]
            [ciste.views :refer [defview]]
            [jiksnu.actions.user-actions :as actions.user]))

(defview #'actions.user/show :as
  [request user]
  {:template false
   :body {:profile (show-section user)
          :updated (:updated user)
          :published (:created user)
          :nickname (:username user)}})
