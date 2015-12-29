(ns jiksnu.modules.core.sections.key-sections
  (:require [ciste.sections :refer [defsection]]
            [ciste.sections.default :refer [show-section index-line]]
            [jiksnu.modules.core.sections :refer [admin-index-block]])
  (:import jiksnu.model.Key))

(defsection show-section [Key :html]
  [key & _]
  ;; TODO: Rdfa
  [:div
   [:p (:n key)]
   [:p (:e key)]])
