(ns jiksnu.views.admin.group-views
  (:use [ciste.sections.default :only [index-section]]
        [ciste.views :only [defview]]
        [jiksnu.actions.admin.group-actions :only [index]]
        [jiksnu.ko :only [*dynamic*]]
        [jiksnu.sections :only [admin-index-section bind-to format-page-info
                                pagination-links with-page]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.sections.group-sections :as sections.like])
  (:import jiksnu.model.Group))

(defview #'index :html
  [request {:keys [items] :as page}]
  {:single true
   :title "Groups"
   :body
   (with-page "groups"
     (pagination-links page)
     (bind-to "items"
       (let [items (if *dynamic* [(Group.)] items)]
         (admin-index-section items page))))})

(defview #'index :viewmodel
  [request {:keys [items] :as page}]
  {:body {:title "Groups"
          :pages {:default (format-page-info page)}
          :groups (admin-index-section items page)}})
