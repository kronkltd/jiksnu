(ns jiksnu.modules.web.routes.conversation-routes
  (:require [ciste.initializer :refer [definitializer]]
            [jiksnu.actions.activity-actions :as activity]
            [jiksnu.actions.conversation-actions :as conversation])
  (:import jiksnu.model.Conversation))

(defn routes
  []
  [[[:get "/main/conversations.:format"] #'conversation/index]
   [[:get "/main/conversations"] #'conversation/index]
   [[:get "/main/conversations/:id.:format"]  #'conversation/show]
   [[:get "/main/conversations/:id"]  #'conversation/show]
   [[:get "/model/conversations/:id"] #'conversation/show]
   ])

(defn pages
  []
  [
   [{:name "conversations"}    {:action #'conversation/index}]
   ])

(defn sub-pages
  []
  [
   [{:type Conversation
     :name "activities"}    {:action #'activity/fetch-by-conversation}]
   ])
