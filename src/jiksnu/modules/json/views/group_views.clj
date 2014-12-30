(ns jiksnu.modules.json.views.group-views
  (:require [ciste.model :as cm]
            [ciste.views :refer [defview]]
            [ciste.sections.default :refer [index-section add-form
                                            show-section]]
            [clojure.tools.logging :as log]
            [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.actions.group-actions :as actions.group]
            [jiksnu.ko :refer [*dynamic*]]
            [jiksnu.modules.web.sections :refer [bind-to format-page-info
                                                 pagination-links with-page
                                                 with-sub-page]]
            [jiksnu.modules.web.sections.group-sections :as sections.group])
  (:import jiksnu.model.Group))

(defview #'actions.group/index :json
  [request {:keys [items] :as page}]
  {:body
   {:items (index-section items page)}})
