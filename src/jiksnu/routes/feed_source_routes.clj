(ns jiksnu.routes.feed-source-routes
  (:use [clojurewerkz.route-one.core :only [add-route! named-path]])
  (:require [jiksnu.actions.feed-source-actions :as feed-source]))

(add-route! "/main/feedSources/:id" {:named "show feed-source"})

(defn routes
  []
  [
   [[:get (named-path "show feed-source")]
    #'feed-source/show]

   [[:get (str (named-path "show feed-source") ".:format")]
    #'feed-source/show]

   [[:get "/main/push/callback"]
    #'feed-source/process-updates]

   [[:get "/model/feedSources/:id.:format"] #'feed-source/show]
   ])
