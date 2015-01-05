(ns jiksnu.modules.command.routes.auth-routes
  (:require [ciste.commands :refer [add-command!]]
            [jiksnu.actions.auth-actions :as actions.auth]))

(add-command! "auth"   #'actions.auth/login)
(add-command! "whoami" #'actions.auth/whoami)
