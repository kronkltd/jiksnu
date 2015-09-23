(ns jiksnu.modules.web.routes.subscription-routes
  (:require [jiksnu.actions.subscription-actions :as sub]))

(defn pages
  []
  [
   [{:name "subscriptions"}         {:action #'sub/index}]
   ])


