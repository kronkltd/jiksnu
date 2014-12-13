(ns jiksnu.modules.core.sections.group-sections
  (:use [ciste.sections :only [defsection]]
        [ciste.sections.default :only [actions-section add-form delete-button edit-button
                                       index-block index-line index-section show-section
                                       update-button]]
        [jiksnu.ko :only [*dynamic*]]
        [jiksnu.modules.core.sections :only [admin-show-section admin-index-block admin-index-line
                                             admin-index-section]]
        [jiksnu.modules.web.sections :only [action-link bind-property control-line display-property
                                            dropdown-menu]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.model.user :as model.user]
            [jiksnu.session :as session])
  (:import jiksnu.model.Group))

;; admin-index-block

(defsection admin-index-block [Group]
  [groups & [page]]
  (map #(admin-index-line % page) groups))

(defsection admin-index-block [Group :viewmodel]
  [items & [page]]
  (->> items
       (map (fn [m] (index-line m page)))
       doall))

;; admin-index-line

(defsection admin-index-line [Group]
  [item & [page]]
  (admin-show-section item page))

;; admin-index-section

(defsection admin-index-section [Group]
  [items & [page]]
  (admin-index-block items page))

;; admin-show-section

(defsection admin-show-section [Group]
  [item & [page]]
  (show-section item page))

(defsection index-block [Group :viewmodel]
  [items & [page]]
  (->> items
       (map (fn [m] (index-line m page)))
       doall))

;; index-line

(defsection index-line [Group]
  [group & [page]]
  (show-section group page))

;; index-section

(defsection index-section [Group]
  [items & [page]]
  (index-block items page))

;; show-section

(defsection show-section [Group]
  [item & [page]]
  item)

(defn user-groups
  [user]

  )
