(ns jiksnu.http.view.domain-view
  (:use ciste.core
        ciste.view
        jiksnu.http.controller.domain-controller)
  )

(defview #'index :html
  [request domains]
  {:body
   [:div
    [:p "index domains"]
    [:table
     [:tr
      [:th "Name"]
      [:th "OSW Enabled?"]
      ]
     (map
      (fn [row]
        [:tr
         [:td
          [:a {:href (str "/domains/" (:_id row))} (:_id row)]]
         [:td (:osw row)]
         ]
        )
      domains)
     ]
    ]
   }
  )


(defview #'show :html
  [request domain]
  {:body
   [:div
    [:p (:_id domain)]
    [:p (:osw domain)]
    ]
   }
  )
