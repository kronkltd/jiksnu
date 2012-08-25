(ns jiksnu.routes.feed-source-routes
  (:require [jiksnu.actions.feed-source-actions :as feed-source]))

(defn routes
  []
  [[[:get    "/main/push/callback"]  #'feed-source/process-updates]])
