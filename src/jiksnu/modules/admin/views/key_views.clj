(ns jiksnu.modules.admin.views.key-views
  (:use [ciste.views :only [defview]]
        [jiksnu.modules.admin.actions.key-actions :only [index]]
        [jiksnu.modules.core.sections :only [admin-index-section]]
        [jiksnu.modules.web.sections :only [format-page-info pagination-links with-page]]))

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

