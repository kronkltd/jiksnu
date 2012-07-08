(ns jiksnu.routes.domain-routes
  (:require [jiksnu.actions.domain-actions :as domain])
  )

(defn routes
  []
  [
   [[:get    "/.well-known/host-meta"]                       #'domain/host-meta]
   [[:get    "/.well-known/host-meta.:format"]               #'domain/host-meta]
   [[:get    "/main/domains.:format"]                        #'domain/index]
   [[:get    "/main/domains"]                                #'domain/index]
   [[:get    "/main/domains/:id.:format"]                    #'domain/show]
   [[:get    "/main/domains/:id"]                            #'domain/show]
   [[:delete "/main/domains/*"]                              #'domain/delete]
   [[:post   "/main/domains/:id/discover"]                   #'domain/discover]
   [[:post   "/main/domains/:id/edit"]                       #'domain/edit-page]
   [[:post   "/main/domains"]                                #'domain/find-or-create]

   ]
  
  )
