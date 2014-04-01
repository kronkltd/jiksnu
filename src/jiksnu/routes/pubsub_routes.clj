(ns jiksnu.routes.pubsub-routes
  (:require [jiksnu.actions.pubsub-actions :as pubsub]
            [jiksnu.routes.helpers :refer [add-route! named-path]]))

(add-route! "/main/push/hub" {:named "hub dispatch"})

(defn routes
  []
  [[[:get  (named-path "hub dispatch")] #'pubsub/hub-dispatch]
   [[:post (named-path "hub dispatch")] #'pubsub/hub-dispatch]
   ;; [[:post   "/users/:id/push/subscribe"] #'pubsub/subscribe]
   ])

