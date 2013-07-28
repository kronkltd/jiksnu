(ns jiksnu.routes.pubsub-routes
  (:use [ciste.initializer :only [definitializer]]
        [ciste.loader :only [require-namespaces]]
        [clojurewerkz.route-one.core :only [add-route! named-path]]
        [jiksnu.routes.helpers :only [formatted-path]])
  (:require [jiksnu.actions.pubsub-actions :as pubsub]))

(add-route! "/main/push/hub" {:named "hub dispatch"})

(defn routes
  []
  [[[:get  (named-path "hub dispatch")] #'pubsub/hub-dispatch]
   [[:post (named-path "hub dispatch")] #'pubsub/hub-dispatch]
   ;; [[:post   "/users/:id/push/subscribe"] #'pubsub/subscribe]
   ])

(definitializer
  (require-namespaces
   ["jiksnu.filters.pubsub-filters"
    ;; "jiksnu.triggers.pubsub-triggers"
    "jiksnu.views.pubsub-views"]))
