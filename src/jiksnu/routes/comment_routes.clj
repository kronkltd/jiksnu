(ns jiksnu.routes.comment-routes
  (:use [ciste.initializer :only [definitializer]]
        [ciste.loader :only [require-namespaces]])
  (:require [jiksnu.actions.comment-actions :as comment]))

(defn routes
  []
  [[[:get    "/notice/:id/comment"]                          #'comment/new-comment]
   [[:post   "/notice/:id/comments"]                         #'comment/add-comment]
   [[:post   "/notice/:id/comments/update"]                  #'comment/fetch-comments]])
