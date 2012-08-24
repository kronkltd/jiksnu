(ns jiksnu.routes.group-routes
  (:use [clojurewerkz.route-one.core :only [add-route! named-path]])
  (:require [jiksnu.actions.group-actions :as group]))

(add-route! "/groups" {:named "index groups"})

(defn routes
  []
  [
     [[:get    "/api/statusnet/app/memberships/:id.:format"]   #'group/user-list]
     [[:get    "/groups.:format"]                              #'group/index]
     [[:get    (named-path "index groups")]                                      #'group/index]
     [[:post   "/groups"]                                      #'group/create]
     [[:get    "/groups/new"]                                  #'group/new-page]
     [[:get    "/groups/:name/edit"]                           #'group/edit-page]
     ;; [[:get    "/search/group"]                                 #'group/search-page]
     ;; [[:post   "/search/group"]                                 #'group/search]
     [[:get    "/:username/groups.:format"]                    #'group/user-list]
     [[:get    "/:username/groups"]                            #'group/user-list]
  

   ]
  )
