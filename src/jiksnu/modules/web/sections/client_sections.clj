(ns jiksnu.modules.web.sections.client-sections
  (:require [ciste.sections :refer [defsection]]
            [ciste.sections.default :refer [actions-section]]
            [clojure.tools.logging :as log]
            [jiksnu.modules.core.sections :refer [admin-index-line
                                                  admin-index-block]])
  (:import jiksnu.model.Client))

(defsection admin-index-block [Client :html]
  [items & [options & _]]
  [:table.table
   [:thead
    [:tr
     [:th "Id"]
     [:th "Created"]
     [:th "Updated"]]]
   [:tbody
    (map admin-index-line items)]])

(defsection admin-index-line [Client :html]
  [item & [options & _]]
  [:tr {:data-model "client"
        :data-id "{{client.id}}"
        :ng-repeat "client in clients"}
   [:td "{{client.id}}"]
   [:td "{{client.created}}"]
   [:td "{{client.updated}}"]
   #_[:td (actions-section activity)]])

