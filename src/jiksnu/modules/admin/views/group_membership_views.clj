(ns jiksnu.modules.admin.views.group-membership-views
  (:require [ciste.views :refer [defview]]
            [clojure.tools.logging :as log]
            [jiksnu.modules.admin.actions.group-membership-actions :refer [index]]
            [jiksnu.modules.core.sections :refer [admin-index-section]]
            [jiksnu.modules.web.sections :refer [format-page-info]])
  (:import jiksnu.model.GroupMembership))

(defview #'index :html
  [request {:keys [items] :as page}]
  {:single true
   :title "Groups Memberships"
   :body (admin-index-section items page)})

(defview #'index :viewmodel
  [request {:keys [items] :as page}]
  {:body {:title "Groups Memberships"
          :pages {:groups (format-page-info page)}
          :groups (admin-index-section items page)}})
