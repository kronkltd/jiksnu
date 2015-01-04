(ns jiksnu.modules.json.sections.domain-sections
  (:require [ciste.core :refer [with-format]]
            [ciste.sections :refer [defsection]]
            [ciste.sections.default :refer [actions-section delete-button index-block
                                            index-line show-section uri]]
            [clojure.tools.logging :as log]
            [jiksnu.session :refer [current-user is-admin?]]
            [jiksnu.modules.core.sections :refer [admin-index-block admin-index-line]]
            [jiksnu.modules.web.sections :refer [action-link bind-to dropdown-menu]]
            [jiksnu.namespace :as ns]
            [jiksnu.session :as session])
  (:import jiksnu.model.Domain))

(defsection show-section [Domain :json]
  [item & [page]]
  item)
