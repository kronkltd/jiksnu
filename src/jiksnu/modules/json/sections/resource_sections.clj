(ns jiksnu.modules.json.sections.resource-sections
  (:require [ciste.sections :refer [defsection]]
            [ciste.sections.default :refer [show-section]]
            [taoensso.timbre :as log])
  (:import jiksnu.model.Resource))

(defsection show-section [Resource :json]
  [user & _]
  user)
