(ns jiksnu.routes.pubsub-routes
  (:require [jiksnu.actions.pubsub-actions :as pubsub]))

(defn routes
  []
  [[[:get    "/main/push/hub"]            #'pubsub/hub-dispatch]
   [[:post   "/main/push/hub"]            #'pubsub/hub-dispatch]
   ;; [[:post   "/users/:id/push/subscribe"] #'pubsub/subscribe]
   ])
