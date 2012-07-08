(ns jiksnu.routes.conversation-routes
  (:require [jiksnu.actions.conversation-actions :as conversation]))

(defn routes
  []
  [
     [[:get    "/main/conversations"]                          #'conversation/index]
     [[:get    "/main/conversations.:format"]                  #'conversation/index]
     [[:get    "/main/conversations/:id"]                      #'conversation/show]
     [[:get    "/main/conversations/:id.:format"]              #'conversation/show]

   ]
  
  )
