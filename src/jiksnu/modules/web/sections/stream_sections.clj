(ns jiksnu.modules.web.sections.stream-sections
  (:require [ciste.sections :refer [defsection]]
            [clojure.tools.logging :as log]
            [jiksnu.actions.stream-actions :as actions.stream]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.ko :refer [*dynamic*]]
            [jiksnu.modules.core.sections :refer [admin-index-block
                                                  admin-index-section]]
            [jiksnu.modules.web.sections :refer [display-property]])
  (:import jiksnu.model.Stream))

(defsection admin-index-block [Stream :html]
  [items & [page]]
  [:table.table
   [:thead
    [:tr
     [:th "Name"]]]
   [:tbody {:data-bind "foreach: items"}
    (for [item items]
      [:tr {:data-bind "stream"}
       [:td (display-property item :name)]

       ]
      )

    ]
   ]

  )

(defsection admin-index-section [Stream :html]
  [items & [page]]
  (admin-index-block items page)
  )

(defn streams-widget
  [user]
  (let [page (if *dynamic*
               {:items [(Stream.)]}
               (actions.stream/fetch-by-user user))]
    [:div
     [:h3 "Streams " (display-property page :totalRecords)]
     [:ul {:data-bind "foreach: items"}
      (for [item (:items page)]
        [:li {:data-model "stream"}
         (display-property item :name)])
      ]
     ]
    )
  )
