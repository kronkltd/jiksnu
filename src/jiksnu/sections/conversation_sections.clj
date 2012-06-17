(ns jiksnu.sections.conversation-sections
  (:use [ciste.model :only [implement]]
        [ciste.sections :only [defsection]]
        [ciste.sections.default :only [delete-button full-uri uri title index-line
                                       index-section link-to show-section]]
        [jiksnu.sections :only [control-line]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.model.conversation :as model.conversation])
  (:import jiksnu.model.Conversation))

(defn admin-index-section
  [page]
  (index-section (:items page) page))

