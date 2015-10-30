(ns jiksnu.modules.admin.views.group-membership-views
  (:require [ciste.views :refer [defview]]
            [jiksnu.modules.admin.actions.group-membership-actions :refer [index]]
            [jiksnu.modules.core.sections :refer [admin-index-section]])
  (:import jiksnu.model.GroupMembership))

(defview #'index :html
  [request {:keys [items] :as page}]
  {:single true
   :title "Groups Memberships"
   :body (admin-index-section items page)})
