(ns jiksnu.modules.admin.views.activity-views
  (:require [ciste.views :refer [defview]]
            [taoensso.timbre :as log]
            [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.modules.admin.actions.activity-actions :refer [index]]
            [jiksnu.modules.core.sections :refer [admin-index-section
                                                  format-page-info]]))

(defview #'index :html
  [request {:keys [items] :as page}]
  {:title "Activities"
   :single true
   :body (admin-index-section items page)})
