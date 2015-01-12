(ns jiksnu.modules.web.routes.tag-routes
  (:require [jiksnu.actions.tag-actions :as tag]))

(defn routes
  []
  [
   [[:get    "/tags/:name.:format"] #'tag/show]
   [[:get    "/tags/:name"]         #'tag/show]
   ;; [[:get    "/tags"]               #'tag/index]
   ])
