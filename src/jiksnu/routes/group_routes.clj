(ns jiksnu.routes.group-routes
  (:require [jiksnu.actions.group-actions :as group]))

(defn routes
  []
  [
     [[:get    "/api/statusnet/app/memberships/:id.:format"]   #'group/user-list]
     [[:get    "/groups"]                                      #'group/index]
     [[:post   "/groups"]                                      #'group/create]
     [[:get    "/groups/new"]                                  #'group/new-page]
     [[:get    "/groups/:name"]                                #'stream/group-timeline]
     [[:get    "/groups/:name/edit"]                           #'group/edit-page]
     ;; [[:get    "/search/group"]                                 #'group/search-page]
     ;; [[:post   "/search/group"]                                 #'group/search]
     [[:get    "/:username/groups"]                            #'group/user-list]
  

   ]
  )
