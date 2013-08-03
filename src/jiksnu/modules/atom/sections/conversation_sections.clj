(ns jiksnu.modules.atom.sections.conversation-sections
  (:use [ciste.sections :only [defsection]]
        [ciste.sections.default :only [index-block index-section]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.actions.activity-actions :as actions.activity])
  (:import jiksnu.model.Conversation))

;; index-section

(defsection index-section [Conversation :atom]
  [items & [page]]
  (let [ids (map :_id items)
        page (actions.activity/fetch-by-conversations ids)]
    (index-block (:items page) page)))
