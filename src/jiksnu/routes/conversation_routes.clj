(ns jiksnu.routes.conversation-routes
  (:require [ciste.initializer :refer [definitializer]]
            [jiksnu.actions.activity-actions :as activity]
            [jiksnu.actions.conversation-actions :as conversation]
            [jiksnu.routes.helpers :refer [add-route! named-path formatted-path]])
  (:import jiksnu.model.Conversation))

(add-route! "/main/conversations"     {:named "index conversations"})
(add-route! "/main/conversations/:id" {:named "show conversation"})
(add-route! "/model/conversations/:id" {:named "conversation model"})

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
