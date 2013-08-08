(ns jiksnu.routes.like-routes
  (:require [jiksnu.actions.like-actions :as like]))

(defn routes
  []
  [[[:post   "/likes/:id/delete"] #'like/delete]
   [[:post   "/notice/:id/like"]  #'like/like-activity]])

