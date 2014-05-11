(ns jiksnu.modules.web.routes.site-routes
  (:require [jiksnu.actions.site-actions :as site]))

(defn routes
  []
  [[[:get "/api/statusnet/app/service"] #'site/service]
   [[:get "/rsd"]     {:action #'site/rsd
                                       :format :xml}]
   [[:get "/main/stats.:format"]   #'site/get-stats]
   [[:get "/main/stats"]   #'site/get-stats]])

