(ns jiksnu.routes.favorite-routes
  (:use [ciste.initializer :only [definitializer]]
        [ciste.loader :only [require-namespaces]])
  (:require [jiksnu.actions.favorite-actions :as favorite]))

(defn routes
  []
  [[[:get    "/api/statusnet/app/favorites/:id.:format"]     #'favorite/user-list]])
