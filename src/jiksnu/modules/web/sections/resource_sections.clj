(ns jiksnu.modules.web.sections.resource-sections
  (:require [ciste.sections :refer [defsection]]
            [ciste.sections.default :refer [actions-section delete-button index-line
                                            index-block index-line link-to
                                            show-section update-button]]
            [clojure.tools.logging :as log]
            [jiksnu.modules.core.sections :refer [admin-index-line]]
            [jiksnu.modules.web.sections :refer [action-link dropdown-menu]]
            [jiksnu.modules.web.sections.link-sections :as sections.link]
            [jiksnu.session :as session])
  (:import jiksnu.model.Domain
           jiksnu.model.FeedSource
           jiksnu.model.Resource))

(defsection link-to [Resource :html]
  [source & _]
  [:a {:href "/resources/{{resource.id}}"}
   "{{resource.topic}}"])

