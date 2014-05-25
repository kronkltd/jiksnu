(ns jiksnu.modules.admin.sections.group-sections
  (:require [ciste.sections :refer [defsection]]
            [ciste.sections.default :refer [actions-section delete-button]]
            [clojure.tools.logging :as log]
            [jiksnu.ko :refer [*dynamic*]]
            [jiksnu.modules.core.sections :refer [admin-index-block
                                                  admin-index-line]]
            [jiksnu.modules.web.sections :refer [display-property]])
  (:import jiksnu.model.Group
           jiksnu.model.User))

(defsection admin-index-block [Group :html]
  [groups & [options & _]]
  [:table.table.groups
   [:thead
    [:tr
     [:th "Name"]
     [:th "Full Name"]
     [:th "Homepage"]]]
   [:tbody (when *dynamic* {:data-bind "foreach: items"})
    (map #(admin-index-line % options) groups)]])

(defsection admin-index-line [Group :html]
  [group & [options & _]]
  [:tr (merge {:data-model "group"}
              (if *dynamic*
                {}
                {:data-id (:_id group)}))
   [:td (display-property group :nickname)]
   [:td (display-property group :fullname)]
   [:td (display-property group :homepage)]
   [:td (actions-section group)]])

