(ns jiksnu.routes.subscription-routes
  (:require [jiksnu.actions.subscription-actions :as sub]))

(defn routes
  []
  [
   [[:get    "/api/statusnet/app/subscriptions/:id.:format"] #'sub/get-subscriptions]
   [[:get    "/main/ostatus"]                                #'sub/ostatus]
   [[:get    "/main/ostatussub"]                             #'sub/ostatussub]
   [[:post   "/main/ostatussub"]                             #'sub/ostatussub-submit]
   [[:post   "/main/subscribe"]                              #'sub/subscribe]
   [[:post   "/main/unsubscribe"]                            #'sub/unsubscribe]
   [[:delete "/subscriptions/:id"]                           #'sub/delete]
   [[:get    "/users/:id/subscriptions.:format"]             #'sub/get-subscriptions]
   [[:get    "/users/:id/subscriptions"]                     #'sub/get-subscriptions]
   [[:get    "/users/:id/subscribers.:format"]               #'sub/get-subscribers]
   [[:get    "/users/:id/subscribers"]                       #'sub/get-subscribers]
   [[:post   "/users/:id/unsubscribe"]                       #'sub/unsubscribe]
   [[:get    "/:username/subscribers.:format"]               #'sub/get-subscribers]
   [[:get    "/:username/subscribers"]                       #'sub/get-subscribers]
   [[:get    "/:username/subscriptions.:format"]             #'sub/get-subscriptions]
   [[:get    "/:username/subscriptions"]                     #'sub/get-subscriptions]
   [[:post   "/users/:subscribeto/subscribe.:format"]        #'sub/subscribe]
   [[:post   "/users/:subscribeto/subscribe"]                #'sub/subscribe]
 

   ]
  )
