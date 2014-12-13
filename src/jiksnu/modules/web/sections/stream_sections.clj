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

(defn add-form
  [user]
  [:form
   (merge {:method "post" }
          (if *dynamic*
            {:action "/users/{{user.id}}/streams"}
            {:action (format "/users/%s/streams" (:_id user))}))
   [:input {:type "text" :name "name"}]
   [:input {:type "submit"}]])

(defsection admin-index-block [Stream :html]
  [items & [page]]
  [:table.table
   [:thead
    [:tr
     [:th "Name"]]]
   [:tbody

    (for [item items]
      [:tr {:data-model "stream"
            :ng-repeat "conversation in conversations"
            }
       [:td (display-property item :name)]])]])

(defsection admin-index-section [Stream :html]
  [items & [page]]
[:table.table
   [:thead
    [:tr
     [:th "Name"]]]
   [:tbody

    (for [item items]
      [:tr {:data-model "stream"
            :ng-repeat "conversation in conversations"
            }
       [:td (display-property item :name)]])]]




 )


(defn streams-widget
  [user]
  (let [page (if *dynamic*
               {:items [(Stream.)]}
               (actions.stream/fetch-by-user user))]
    [:div
     [:h3 "Streams " (display-property page :totalRecords)]
     [:ul
      (for [item (:items page)]
        [:li {:ng-repeat "stream in streams"}
         (display-property item :name)])]]))
