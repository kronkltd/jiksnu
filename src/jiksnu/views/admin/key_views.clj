(ns jiksnu.views.admin.key-views
  (:use [ciste.sections.default :only [index-section]]
        [ciste.views :only [defview]]
        [jiksnu.actions.admin.key-actions :only [index]]))

(defview #'index :html
  [request response]
  {:body "foo"
   :title "Keys"})
