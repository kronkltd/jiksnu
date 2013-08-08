(ns jiksnu.routes.pubsub-routes
  (:use [clojurewerkz.route-one.core :only [add-route! named-path]])
  (:require [jiksnu.actions.pubsub-actions :as pubsub]))

(add-route! "/main/push/hub" {:named "hub dispatch"})

(defn routes
  []
  [[[:get  (named-path "hub dispatch")] #'pubsub/hub-dispatch]
   [[:post (named-path "hub dispatch")] #'pubsub/hub-dispatch]
   ;; [[:post   "/users/:id/push/subscribe"] #'pubsub/subscribe]
   ])

