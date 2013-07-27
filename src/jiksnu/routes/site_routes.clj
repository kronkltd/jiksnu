(ns jiksnu.routes.site-routes
  (:use [ciste.commands :only [add-command!]]
        [ciste.initializer :only [definitializer]]
        [ciste.loader :only [require-namespaces]]
        [clojurewerkz.route-one.core :only [add-route! named-path]]
        [jiksnu.routes.helpers :only [formatted-path]])
  (:require [jiksnu.actions.site-actions :as site]))

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

(add-command! "get-environment" #'site/get-environment)
(add-command! "get-stats"       #'site/get-stats)
;; (add-command! "get-load"        #'site/get-load)
(add-command! "config"          #'site/get-config)
(add-command! "ping"            #'site/ping)

(definitializer
  (require-namespaces
   ["jiksnu.filters.site-filters"
    "jiksnu.views.site-views"]))
