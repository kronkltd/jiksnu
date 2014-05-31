(ns jiksnu.modules.web.routes.group-membership-routes
  (:require [jiksnu.actions.group-membership-actions :as actions.group-membership])
  )

(defn pages
  []
  [
   [{:name "group-membershipss"}    {:action #'actions.group-membership/index}]
   ])

