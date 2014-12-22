(ns jiksnu.modules.web.views.site-views
  (:require [ciste.model :as cm]
            [ciste.views :refer [defview]]
            [ciste.sections.default :refer [index-section add-form
                                            show-section]]
            [clojure.tools.logging :as log]
            [jiksnu.actions.site-actions :as actions.site]
            [jiksnu.modules.web.sections :refer [bind-to format-page-info
                                                 pagination-links with-page
                                                 with-sub-page]]
            [jiksnu.modules.web.sections.group-sections :as sections.group])
  (:import jiksnu.model.Group))

(defview #'actions.site/status :json
  [request response]
  {:body response}
  )
