(ns jiksnu.modules.command.routes.site-routes
  (:require [ciste.commands :refer [add-command!]]
            [jiksnu.actions.site-actions :as actions.site]))

(add-command! "get-environment" #'actions.site/get-environment)
(add-command! "get-stats"       #'actions.site/get-stats)
;; (add-command! "get-load"        #'actions.site/get-load)
(add-command! "config"          #'actions.site/get-config)
(add-command! "ping"            #'actions.site/ping)

