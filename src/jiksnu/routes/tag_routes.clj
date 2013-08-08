(ns jiksnu.routes.tag-routes
  (:use [ciste.initializer :only [definitializer]]
        [ciste.loader :only [require-namespaces]])
  (:require [jiksnu.actions.tag-actions :as tag]))

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
