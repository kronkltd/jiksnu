(ns jiksnu.modules.json.sections.domain-sections
  (:require [ciste.sections :refer [defsection]]
            [ciste.sections.default :refer [show-section]]
            [clojure.tools.logging :as log])
  (:import jiksnu.model.Domain))

(defsection show-section [Domain :json]
  [item & [page]]
  item)
