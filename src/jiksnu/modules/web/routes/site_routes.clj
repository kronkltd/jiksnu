(ns jiksnu.modules.web.routes.site-routes
  (:require [jiksnu.actions.site-actions :as site]))

(defn routes
  []
  [[[:get "/api/statusnet/app/service"] #'site/service]
   [[:get "/rsd.:format"]               #'site/rsd]
   [[:get "/rsd"]                       {:action #'site/rsd :format :xml}]
   [[:get "/status"]                    {:action #'site/status :format :json}]
   [[:get "/main/stats.:format"]        #'site/get-stats]
   [[:get "/main/stats"]                #'site/get-stats]])

