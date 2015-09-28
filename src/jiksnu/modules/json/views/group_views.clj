(ns jiksnu.modules.json.views.group-views
  (:require [ciste.views :refer [defview]]
            [ciste.sections.default :refer [index-section]]
            [clojure.tools.logging :as log]
            [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.actions.group-actions :as actions.group]
            [jiksnu.modules.core.sections :refer [format-page-info]])
  (:import jiksnu.model.Group))

(defview #'actions.group/index :json
  [request {:keys [items] :as page}]
  {:body
   {:items (index-section items page)}})
