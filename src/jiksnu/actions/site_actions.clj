(ns jiksnu.actions.site-actions
  (:use [ciste.commands :only [add-command!]]
        [ciste.config :only [definitializer]]
        [ciste.core :only [defaction]]
        [ciste.runner :only [require-namespaces]])
  (:require [inflections.core :as inf]))

(defaction service
  [id]
  ;; get user
  true
  )

(defaction rsd
  []
  true
  )

(defaction get-stats
  []
  (->> [:activities :conversations :domains
        :groups :feed-sources :feed-subscriptions
        :subscriptions :users]
       (map
          (fn [k]
            (let [namespace-sym (symbol (str "jiksnu.model." (inf/singular (name k))))
                  sym (intern (the-ns namespace-sym) (symbol "count-records"))]
              [k (sym)])))
       (into {})))

(add-command! "get-stats" #'get-stats)

(definitializer
  (require-namespaces
   ["jiksnu.filters.site-filters"
    "jiksnu.views.site-views"]))
