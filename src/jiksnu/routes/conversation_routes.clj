(ns jiksnu.routes.conversation-routes
  (:use [ciste.initializer :only [definitializer]]
        [ciste.loader :only [require-namespaces]]
        [clojurewerkz.route-one.core :only [add-route! named-path]]
        [jiksnu.routes.helpers :only [formatted-path]])
  (:require [jiksnu.actions.activity-actions :as activity]
            [jiksnu.actions.conversation-actions :as conversation])
  (:import jiksnu.model.Conversation))

(add-route! "/main/conversations"     {:named "index conversations"})
(add-route! "/main/conversations/:id" {:named "show conversation"})
(add-route! "/model/conversations/:id" {:named "conversation model"})

(defn routes
  []
  [[[:get (formatted-path "index conversations")] #'conversation/index]
   [[:get (named-path     "index conversations")] #'conversation/index]
   [[:get (formatted-path "show conversation")]  #'conversation/show]
   [[:get (named-path     "show conversation")]  #'conversation/show]
   [[:get (formatted-path "conversation model")] #'conversation/show]
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


(definitializer
  (require-namespaces
   ["jiksnu.filters.conversation-filters"
    "jiksnu.triggers.conversation-triggers"
    "jiksnu.sections.conversation-sections"
    "jiksnu.views.conversation-views"]))
