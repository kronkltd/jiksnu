(ns jiksnu.modules.web.sections.stream-sections
  (:require [clojure.tools.logging :as log]
            [jiksnu.actions.stream-actions :as actions.stream]
            [jiksnu.actions.user-actions :as actions.user]))

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
