(ns jiksnu.routes.tag-routes
  (:require [ciste.initializer :refer [definitializer]]
            [ciste.loader :refer [require-namespaces]]
            [jiksnu.actions.tag-actions :as tag]))

(defn routes
  []
  [
   [[:get    "/tags/:name.:format"] #'tag/show]
   [[:get    "/tags/:name"]         #'tag/show]
   ;; [[:get    "/tags"]               #'tag/index]
   ])

;; (definitializer
;;   (require-namespaces
;;    ["jiksnu.modules.core.filters.tag-filters"
;;     "jiksnu.modules.core.views.tag-views"]))
