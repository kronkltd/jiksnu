(ns jiksnu.modules.web.sections.client-sections
  (:require [ciste.core :refer [with-format]]
            [ciste.sections :refer [defsection]]
            [ciste.sections.default :refer [actions-section add-form delete-button edit-button
                                            show-section-minimal show-section link-to uri title
                                            index-block index-line index-section update-button]]
            [clojure.tools.logging :as log]
            [jiksnu.ko :refer [*dynamic*]]
            [jiksnu.modules.core.sections :refer [admin-index-line admin-index-block
                                                 admin-index-section]]
            [jiksnu.modules.web.sections :refer [action-link bind-to control-line display-property
                                                display-timestamp dropdown-menu dump-data
                                                format-links pagination-links]])
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
    (when *dynamic* {:data-bind "foreach: items"})
    (map admin-index-line items)]])

(defsection admin-index-line [Client :html]
  [item & [options & _]]
  [:tr (merge {:data-model "client"}
              (when-not *dynamic*
                { :data-id (:_id item)}))
   [:td (display-property item :_id)]
   [:td (display-timestamp item :created)]
   [:td (display-timestamp item :updated)]
   #_[:td (actions-section activity)]])

