(ns jiksnu.routes.salmon-routes
  (:require [jiksnu.actions.salmon-actions :as salmon]))

(defn routes
  []
  [[[:post "/main/salmon/user/:id"] #'salmon/process]])
