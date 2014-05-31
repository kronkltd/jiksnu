(ns jiksnu.modules.admin.views.group-membership-views
  (:use [ciste.views :only [defview]]
        [jiksnu.modules.admin.actions.group-membership-actions :only [index]]
        [jiksnu.ko :only [*dynamic*]]
        [jiksnu.modules.core.sections :only [admin-index-section]]
        [jiksnu.modules.web.sections :only [format-page-info pagination-links with-page]])
  (:require [clojure.tools.logging :as log])
  (:import jiksnu.model.GroupMembership))

(defview #'index :html
  [request {:keys [items] :as page}]
  (let [items (if *dynamic* [(GroupMembership.)] items)]
    {:single true
     :title "Groups Memberships"
     :body (with-page "group-memberships"
             (pagination-links page)
             (admin-index-section items page))}))

(defview #'index :viewmodel
  [request {:keys [items] :as page}]
  {:body {:title "Groups Memberships"
          :pages {:groups (format-page-info page)}
          :groups (admin-index-section items page)}})
