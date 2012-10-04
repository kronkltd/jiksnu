(ns jiksnu.routes.feed-source-routes
  (:use [clojurewerkz.route-one.core :only [add-route! named-path]]
        [jiksnu.routes.helpers :only [formatted-path]])
  (:require [jiksnu.actions.feed-source-actions :as feed-source]))

(add-route! "/main/feedSources"      {:named "index feed-sources"})
(add-route! "/main/feedSources/:id"  {:named "show feed-source"})
(add-route! "/model/feedSources/:id" {:named "feed-source model"})
(add-route! "/main/push/callback"    {:named "push callback"})

(defn routes
  []
  [
   [[:get (named-path     "show feed-source")]  #'feed-source/show]
   [[:get (formatted-path "show feed-source")]  #'feed-source/show]
   [[:get (named-path     "push callback")]     #'feed-source/process-updates]
   [[:get (formatted-path "feed-source model")] #'feed-source/show]
   ])
