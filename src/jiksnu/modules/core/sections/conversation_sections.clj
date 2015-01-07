(ns jiksnu.modules.core.sections.conversation-sections
  (:require [ciste.sections :refer [defsection]]
            [ciste.sections.default :refer [uri index-block index-section]]
            [clojure.tools.logging :as log]
            [jiksnu.actions.activity-actions :as actions.activity])
  (:import jiksnu.model.Conversation))

(defsection index-section [Conversation :atom]
  [items & [page]]
  (let [ids (map :_id items)
        page (actions.activity/fetch-by-conversations ids)]
    (index-block (:items page) page)))

(defsection uri [Conversation]
  [item & _]
  (format "/main/conversations/%s" (:_id item)))
