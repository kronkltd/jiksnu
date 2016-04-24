(ns jiksnu.modules.core.views.client-views
  (:require [ciste.views :refer [defview]]
            [ciste.sections.default :refer [index-section show-section]]
            [jiksnu.actions.client-actions :as actions.client]))

(defview #'actions.client/index :page
  [request response]
  (taoensso.timbre/info "applying client index view")
  {:body (merge
          response
          {:name (:name request)})})
