(ns jiksnu.modules.web.actions.page-actions
  (:require [jiksnu.predicates :as pred]))

(defn index
  []
  (let [items (map (comp :name first) @pred/*page-matchers*)]
    {:items items}))

(defn show
  [id]
  (when-let [item (some #(when (= id (:name (first %))) %)
                        @pred/*page-matchers*)]
    (let [[{page-name :name} {action :action}] item]
      {:_id page-name
       :ns (str (:ns (meta action)))
       :name (str (:name (meta action)))})))
