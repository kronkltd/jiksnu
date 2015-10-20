(ns jiksnu.modules.json.views.resource-views
  (:require [ciste.views :refer [defview]]
            [ciste.sections.default :refer [index-section]]
            [taoensso.timbre :as log]
            [jiksnu.actions.resource-actions :as actions.resource]))

(defview #' actions.resource/index :json
  [request {:keys [items] :as page}]
  {:body
   {:items (index-section items page)}})
