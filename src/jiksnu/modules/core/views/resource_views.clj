(ns jiksnu.modules.core.views.resource-views
  (:require [ciste.core :refer [with-format]]
            [ciste.views :refer [defview]]
            [ciste.sections.default :refer [show-section]]
            [clojure.tools.logging :as log]
            [jiksnu.actions.resource-actions :as actions.resource]
            [jiksnu.modules.core.sections :refer [format-page-info]]
            [jiksnu.namespace :as ns])
  (:import jiksnu.model.Resource))

(defview #'actions.resource/index :page
  [request response]
  (let [items (:items response)
        response (merge response
                        {:id (:name request)
                         :items (map :_id items)})]
    {:body {:action "page-updated"
            :body response}}))

(defview #'actions.resource/index :viewmodel
  [request {:keys [items] :as page}]
  {:body {:title "Resources"
          :pages {:resources (format-page-info page)}}})

(defview #'actions.resource/show :model
  [request item]
  {:body item})

(defview #'actions.resource/show :viewmodel
  [request item]
  {:body {:targetResource (:_id item)
          :title (or (:title item) "Resource")}})

