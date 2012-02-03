(ns jiksnu.views.admin.subscription-views
  (:use (ciste [views :only [defview]])
        jiksnu.actions.admin.subscription-actions)
  (:require (jiksnu.helpers [subscription-helpers :as helpers.subscription])))

;; (defview #'admin-index :html
;;   [request subscriptions]
;;   {:body (sections.subscription/index-section subscriptions)})

(defview #'index :html
  [request subscriptions]
  {:title "Supscriptions"
   :body
   [:table.table
    [:thead
     [:tr
      [:th "actor"]
      [:th "target"]
      ]
     ]
    [:tbody
     (map
      (fn [subscription]
        [:tr
         [:td "actor" #_(-> activity helpers.activity/get-author :username)]
         [:td "target" #_(:title activity)]
         ]
        )
      subscriptions)
     ]
    ]
   }
  )
