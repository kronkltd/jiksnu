(ns jiksnu.modules.web.sections.stream-sections
  (:require [ciste.sections :refer [defsection]]
            [clojure.tools.logging :as log]
            [jiksnu.actions.stream-actions :as actions.stream]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.modules.core.sections :refer [admin-index-section]]
            )
  (:import jiksnu.model.Stream))

(defsection admin-index-section [Stream :html]
  [items & [page]]
  "TODO: Admin index"
  )

(defn streams-widget
  [user]
  (let [page (log/spy :info (actions.stream/fetch-by-user user))]
    [:div
     [:h3 "Streams"]
     [:ul
      [:li "Stream"]
      ]
     ]
    )
  )
