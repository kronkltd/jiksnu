(ns jiksnu.routes.site-routes
  (:require [jiksnu.actions.site-actions :as site]))

(defn routes
  []
  [[[:get    "/api/statusnet/app/service.:format"]           #'site/service]
   [[:get    "/rsd.xml"]                                     #'site/rsd]
   [[:get    "/main/stats.:format"]                          #'site/get-stats]
   [[:get    "/main/stats"]                                  #'site/get-stats]])
