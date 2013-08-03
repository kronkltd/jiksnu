(ns jiksnu.modules.core.views.admin.group-views
  (:use [ciste.views :only [defview]]
        [jiksnu.actions.admin.group-actions :only [index]]
        [jiksnu.ko :only [*dynamic*]]
        [jiksnu.modules.web.sections :only [admin-index-section format-page-info
                                            pagination-links with-page]])
  (:require [clojure.tools.logging :as log])
  (:import jiksnu.model.Group))

(defview #'index :html
  [request {:keys [items] :as page}]
  (let [items (if *dynamic* [(Group.)] items)]
    {:single true
     :title "Groups"
     :body (with-page "groups"
             (pagination-links page)
             (admin-index-section items page))}))

(defview #'index :viewmodel
  [request {:keys [items] :as page}]
  {:body {:title "Groups"
          :pages {:groups (format-page-info page)}
          :groups (admin-index-section items page)}})
