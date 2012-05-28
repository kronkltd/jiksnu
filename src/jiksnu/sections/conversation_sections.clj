(ns jiksnu.sections.conversation-sections
  (:use [ciste.debug :only [spy]]
        [ciste.model :only [implement]]
        [ciste.sections :only [defsection]]
        [ciste.sections.default :only [delete-button full-uri uri title index-line
                                       index-section link-to show-section]]
        [jiksnu.views :only [control-line]])
  (:require [jiksnu.model.conversation :as model.conversation])
  (:import jiksnu.model.Conversation))

(defn admin-index-section
  [records]
  (index-section records))

(defsection index-section [Conversation :html]

  )
