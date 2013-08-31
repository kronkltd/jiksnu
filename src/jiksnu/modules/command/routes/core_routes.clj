(ns jiksnu.modules.command.routes.core-routes
  (:require [ciste.commands :refer [add-command! command-names]]
            [jiksnu.actions :as actions])
  )

(add-command! "invoke-action" #'actions/invoke-action)
(add-command! "connect"       #'actions/connect)
(add-command! "get-model"     #'actions/get-model)
(add-command! "get-page"      #'actions/get-page)
(add-command! "get-sub-page"  #'actions/get-sub-page)

(add-command! "help" #'command-names)

