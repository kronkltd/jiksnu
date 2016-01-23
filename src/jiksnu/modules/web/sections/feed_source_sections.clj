(ns jiksnu.modules.web.sections.feed-source-sections
  (:require [ciste.sections :refer [defsection]]
            [ciste.sections.default :refer [link-to]]
            [jiksnu.modules.web.sections :refer [action-link bind-to]]))

(defn index-watchers
  [source]
  [:div.watchers
   [:h3 "Watchers {{source.watchers.length}}"]
   (bind-to "watchers"
     [:table.table
      [:tbody
       (let [user {}]
         [:tr {:data-model "user"
               :ng-repeat "watcher in source.watchers"}
          [:td (link-to user)]
          [:td
           (action-link "feed-source" "remove-watcher" (:_id source)
                        {:target (:_id user)
                         :icon "trash"
                         :title "Delete"})]])]])])
