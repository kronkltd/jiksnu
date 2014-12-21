(ns jiksnu.modules.core.sections.conversation-sections
  (:use [ciste.sections :only [defsection]]
        [ciste.sections.default :only [uri index-line index-block index-section]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.namespace :as ns])
  (:import jiksnu.model.Conversation))



(defsection index-block [Conversation :json]
  [items & [page]]
  (map
   (fn [item]
     #_(index-line item page)
     item
     )
   (:items page))
  )

;; index-section

(defsection index-section [Conversation :atom]
  [items & [page]]
  (let [ids (map :_id items)
        page (actions.activity/fetch-by-conversations ids)]
    (index-block (:items page) page)))

(defsection index-section [Conversation :json]
  [items & [page]]
  {"@type" "http://activitystrea.ms/2.0/Collection"
   :totalItems (:totalRecords page)
   :itemsPerPage (:page-size page)
   :items (index-block items page)
     :page (:page page)

   }
  )


;; uri

(defsection uri [Conversation]
  [item & _]
  (format "/main/conversations/%s" (:_id item)))
