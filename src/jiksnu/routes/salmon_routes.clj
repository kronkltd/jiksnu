(ns jiksnu.routes.salmon-routes
  (:use [clojurewerkz.route-one.core :only [add-route! named-path]])
  (:require [jiksnu.actions.salmon-actions :as salmon]))

(add-route! "/main/salmon/user/:id" {:named "user salmon"})

(defn routes
  []
  [[[:post (named-path "user salmon")] #'salmon/process]])

