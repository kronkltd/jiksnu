(ns jiksnu.modules.admin.views.key-views
  (:use [ciste.views :only [defview]]
        [jiksnu.modules.admin.actions.key-actions :only [index]]
        [jiksnu.modules.core.sections :only [admin-index-section
                                             format-page-info]]
        [jiksnu.modules.web.sections :only [pagination-links with-page]]))

(defview #'index :html
  [request {:keys [items] :as response}]
  {:body
   (with-page "keys"
     (list
      (pagination-links response)
      (admin-index-section items response)))
   :title "Keys"})
