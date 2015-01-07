(ns jiksnu.modules.admin.views.activity-views
  (:require [ciste.views :refer [defview]]
            [clojure.tools.logging :as log]
            [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.modules.admin.actions.activity-actions :refer [index]]
            [jiksnu.modules.core.sections :refer [admin-index-section
                                                  format-page-info]]))

(defview #'index :html
  [request {:keys [items] :as page}]
  {:title "Activities"
   :single true
   :body (admin-index-section items page)})

(defview #'index :viewmodel
  [request {:keys [items] :as page}]
  (doall
   {:body
    {:title "Activities"
     :pages {:activities (format-page-info page)}
     :activities (admin-index-section items page)}}))
