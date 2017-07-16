(ns jiksnu.modules.web.actions.page-actions
  (:require [jiksnu.predicates :as pred]
            [taoensso.timbre :as timbre]))

(defn index
  []
  (timbre/debug "Indexing pages")
  (let [items (map (comp :name first) @pred/*page-matchers*)]
    {:items items}))

(defn show
  [id]
  (timbre/debugf "Showing Page: %s" id)
  (when-let [item (some #(when (= id (:name (first %))) %)
                        @pred/*page-matchers*)]
    (let [[{page-name :name} {action :action}] item]
      {:_id page-name
       :ns (str (:ns (meta action)))
       :name (str (:name (meta action)))})))
