(ns jiksnu.modules.command.routes
  (:require [ciste.commands :refer [add-command!]]
            [jiksnu.actions.auth-actions :as actions.auth]
            [jiksnu.actions.site-actions :as actions.site]
            [jiksnu.actions.stream-actions :as actions.stream])  )

(add-command! "auth"   #'actions.auth/login)
(add-command! "whoami" #'actions.auth/whoami)
(add-command! "get-environment" #'actions.site/get-environment)
(add-command! "get-stats"       #'actions.site/get-stats)
;; (add-command! "get-load"        #'actions.site/get-load)
(add-command! "config"          #'actions.site/get-config)
(add-command! "ping"            #'actions.site/ping)
(add-command! "list-activities" #'actions.stream/public-timeline)


