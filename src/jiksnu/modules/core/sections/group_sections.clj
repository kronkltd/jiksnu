(ns jiksnu.modules.core.sections.group-sections
  (:require [ciste.sections :refer [defsection]]
            [ciste.sections.default :refer [actions-section edit-button index-block index-line
                                            index-section show-section update-button]]
            [jiksnu.modules.core.sections :refer [admin-show-section
                                                  admin-index-block
                                                  admin-index-line
                                                 admin-index-section]])
  (:import jiksnu.model.Group))

(defsection admin-index-block [Group]
  [groups & [page]]
  (map #(admin-index-line % page) groups))

(defsection admin-index-line [Group]
  [item & [page]]
  (admin-show-section item page))

(defsection admin-index-section [Group]
  [items & [page]]
  (admin-index-block items page))

(defsection admin-show-section [Group]
  [item & [page]]
  (show-section item page))

(defsection index-line [Group]
  [group & [page]]
  (show-section group page))

(defsection index-section [Group]
  [items & [page]]
  (index-block items page))

(defsection show-section [Group]
  [item & [page]]
  item)

(defn user-groups
  [user])
