(ns jiksnu.routes.like-routes
  (:use [ciste.initializer :only [definitializer]]
        [ciste.loader :only [require-namespaces]])
  (:require [jiksnu.actions.like-actions :as like]))

(defn routes
  []
  [[[:post   "/likes/:id/delete"] #'like/delete]
   [[:post   "/notice/:id/like"]  #'like/like-activity]])

