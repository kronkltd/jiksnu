(ns jiksnu.modules.core.views.admin.key-views
  (:use [ciste.sections.default :only [index-section]]
        [ciste.views :only [defview]]
        [jiksnu.actions.admin.key-actions :only [index]]
        [jiksnu.sections :only [admin-index-section format-page-info pagination-links with-page]]))

(defview #'index :html
  [request {:keys [items] :as response}]
  {:body
   (with-page "keys"
     (list
      (pagination-links response)
      (admin-index-section items response)))
   :title "Keys"})

(defview #'index :viewmodel
  [request {:keys [items] :as page}]
  {:body {:title "Keys"
          :pages {:keys (format-page-info page)}
          :keys (doall (admin-index-section items page))}})

