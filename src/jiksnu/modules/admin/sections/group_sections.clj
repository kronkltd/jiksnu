(ns jiksnu.modules.admin.sections.group-sections
  (:require [ciste.sections :refer [defsection]]
            [ciste.sections.default :refer [actions-section]]
            [clojure.tools.logging :as log]
            [jiksnu.modules.core.sections :refer [admin-index-block
                                                  admin-index-line]])
  (:import jiksnu.model.Group))

(defsection admin-index-block [Group :html]
  [groups & [options & _]]
  [:table.table.groups
   [:thead
    [:tr
     [:th "Name"]
     [:th "Full Name"]
     [:th "Homepage"]]]
   [:tbody
    (map #(admin-index-line % options) groups)]])

(defsection admin-index-line [Group :html]
  [group & [options & _]]
  [:tr {:data-model "group"
        :ng-repeat "group in page.items"
        :data-id "{{group.id}}"}
   [:td "{{group.nickname}}"]
   [:td "{{group.fullname}}"]
   [:td "{{group.homepage}}"]
   [:td (actions-section group)]])

