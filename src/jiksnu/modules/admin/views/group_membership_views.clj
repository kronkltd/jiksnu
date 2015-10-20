(ns jiksnu.modules.admin.views.group-membership-views
  (:require [ciste.views :refer [defview]]
            [taoensso.timbre :as log]
            [jiksnu.modules.admin.actions.group-membership-actions :refer [index]]
            [jiksnu.modules.core.sections :refer [admin-index-section
                                                  format-page-info]])
  (:import jiksnu.model.GroupMembership))

(defview #'index :html
  [request {:keys [items] :as page}]
  {:single true
   :title "Groups Memberships"
   :body (admin-index-section items page)})
