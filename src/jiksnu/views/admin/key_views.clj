(ns jiksnu.views.admin.key-views
  (:use [ciste.sections.default :only [index-section]]
        [ciste.views :only [defview]]
        [jiksnu.actions.admin.key-actions :only [index]]
        [jiksnu.sections :only [admin-index-section]]))

(defview #'index :html
  [request {:keys [items] :as response}]
  {:body (admin-index-section items response)
   :title "Keys"})
