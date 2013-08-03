(ns jiksnu.sections.conversation-sections
  (:use [ciste.sections :only [defsection]]
        [ciste.sections.default :only [uri index-block index-section]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.namespace :as ns])
  (:import jiksnu.model.Conversation))

;; index-section

(defsection index-section [Conversation :atom]
  [items & [page]]
  (let [ids (map :_id items)
        page (actions.activity/fetch-by-conversations ids)]
    (index-block (:items page) page)))

;; uri

(defsection uri [Conversation]
  [item & _]
  (format "/main/conversations/%s" (:_id item)))
