(ns jiksnu.modules.admin.views.group-views
  (:require [ciste.views :only [defview]]
            [clojure.tools.logging :as log]
            [jiksnu.modules.admin.actions.group-actions
             :as actions.admin.groups]
            [jiksnu.modules.core.sections :refer [admin-index-section]]
            [jiksnu.modules.web.sections :refer [format-page-info]]))

(defview #'actions.admin.groups/index :viewmodel
  [request {:keys [items] :as page}]
  {:body {:title "Groups"
          :pages {:groups (format-page-info page)}
          :groups (admin-index-section items page)}})
