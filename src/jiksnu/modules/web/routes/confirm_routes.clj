(ns jiksnu.modules.web.routes.confirm-routes
  (:require [jiksnu.actions :as actions]))

(defn routes
  []
  [
   [[:get  "/main/confirm"]          #'actions/confirm]
   ])
