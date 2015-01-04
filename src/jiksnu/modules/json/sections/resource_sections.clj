(ns jiksnu.modules.json.sections.resource-sections
    (:use [ciste.sections :only [defsection]]
          [ciste.sections.default :only [actions-section delete-button index-line
                                         index-block index-line link-to
                                         show-section update-button]]
          [jiksnu.modules.core.sections :only [admin-index-line]]
          [jiksnu.modules.web.sections :only [action-link dropdown-menu]])
    (:require [clojure.tools.logging :as log]
              [jiksnu.modules.web.sections.link-sections :as sections.link]
              [jiksnu.session :as session])
    (:import jiksnu.model.Domain
             jiksnu.model.FeedSource
             jiksnu.model.Resource))

(defsection show-section [Resource :json]
  [user & _]
  user)
