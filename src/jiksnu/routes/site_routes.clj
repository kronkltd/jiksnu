(ns jiksnu.routes.site-routes
  (:require [jiksnu.actions.site-actions :as site]
            [jiksnu.routes.helpers :refer [add-route! formatted-path named-path]]))

(add-route! "/rsd"                       {:named "rsd"})
(add-route! "/main/stats"                {:named "stats"})
(add-route! "/api/statusnet/app/service" {:named "service"})

(defn routes
  []
  [[[:get (formatted-path "service")] #'site/service]
   [[:get (formatted-path "rsd")]     {:action #'site/rsd
                                       :format :xml}]
   [[:get (formatted-path "stats")]   #'site/get-stats]
   [[:get (named-path     "stats")]   #'site/get-stats]])

