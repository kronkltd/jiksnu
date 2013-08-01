(ns jiksnu.routes.subscription-routes
  (:use [ciste.initializer :only [definitializer]]
        [ciste.loader :only [require-namespaces]]
        [clojurewerkz.route-one.core :only [add-route! named-path]]
        [jiksnu.routes.helpers :only [formatted-path]])
  (:require [jiksnu.actions.subscription-actions :as sub]))

(add-route! "/users/:id/subscribers"   {:named "user subscribers"})
(add-route! "/users/:id/subscriptions" {:named "user subscriptions"})
(add-route! "/model/subscriptions/:id" {:named "subscription model"})

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
   [[:get    (named-path "user subscriptions")]              #'sub/get-subscriptions]
   [[:get    "/users/:id/subscribers.:format"]               #'sub/get-subscribers]
   [[:get    (named-path "user subscribers")]                #'sub/get-subscribers]
   [[:post   "/users/:id/unsubscribe"]                       #'sub/unsubscribe]
   [[:get    "/:username/subscribers.:format"]               #'sub/get-subscribers]
   [[:get    "/:username/subscribers"]                       #'sub/get-subscribers]
   [[:get    "/:username/subscriptions.:format"]             #'sub/get-subscriptions]
   [[:get    "/:username/subscriptions"]                     #'sub/get-subscriptions]
   [[:post   "/users/:subscribeto/subscribe.:format"]        #'sub/subscribe]
   [[:post   "/users/:subscribeto/subscribe"]                #'sub/subscribe]
   [[:get    (formatted-path "subscription model")]          #'sub/show]
   ])

(defn pages
  []
  [
   [{:name "subscriptions"}         {:action #'sub/index}]
   ])


