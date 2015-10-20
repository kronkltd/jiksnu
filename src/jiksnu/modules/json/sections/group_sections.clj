(ns jiksnu.modules.json.sections.group-sections
  (:require [ciste.sections :refer [defsection]]
            [ciste.sections.default :refer [uri index-block index-section]]
            [taoensso.timbre :as log]
            [jiksnu.actions.activity-actions :as actions.activity])
  (:import jiksnu.model.Group))

(defsection index-block [Group :json]
  [items & [page]]
  (map identity (:items page)))

(defsection index-section [Group :json]
  [items & [page]]
  {"@type" "http://activitystrea.ms/2.0/Collection"
   :totalItems (:totalItems page)
   :itemsPerPage (:page-size page)
   :items (index-block items page)
   :page (:page page)})
