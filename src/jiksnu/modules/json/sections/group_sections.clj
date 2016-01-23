(ns jiksnu.modules.json.sections.group-sections
  (:require [ciste.sections :refer [defsection]]
            [ciste.sections.default :refer [uri index-block index-section]]
            [jiksnu.actions.activity-actions :as actions.activity])
  (:import jiksnu.model.Group))

(defsection #'actions.activity/index-block [Group :json]
  [items & [page]]
  (map identity (:items page)))

(defsection #'actions.activity/index-section [Group :json]
  [items & [page]]
  {"@type" "http://activitystrea.ms/2.0/Collection"
   :totalItems (:totalItems page)
   :itemsPerPage (:page-size page)
   :items (index-block items page)
   :page (:page page)})
