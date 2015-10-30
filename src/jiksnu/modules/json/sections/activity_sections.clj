(ns jiksnu.modules.json.sections.activity-sections
  (:require [ciste.core :refer [with-format]]
            [ciste.sections :refer [defsection]]
            [ciste.sections.default :refer [show-section]])
  (:import jiksnu.model.Activity))

(defsection show-section [Activity :json]
  [item & _]
  (with-format :model (show-section item)))
