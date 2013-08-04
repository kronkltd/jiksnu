(ns jiksnu.modules.admin.filters.group-filters
    (:use [ciste.filters :only [deffilter]]
          [jiksnu.modules.admin.actions.group-actions :only [index]]))

(deffilter #'index :http
  [action request]
  (-> request :params
      (select-keys #{:page})
       action))
