(ns jiksnu.modules.web.actions.page-actions
  (:require [jiksnu.predicates :as pred]))

(defn index
  []
  (let [items (map (fn [[{page-name :name} {action :action}]]
                     {:name page-name
                      :action (str action)
                      }
                     )
                   @pred/*page-matchers*
                   )]
    {:items items}))