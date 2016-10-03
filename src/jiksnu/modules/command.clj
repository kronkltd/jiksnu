(ns jiksnu.modules.command
  (:require [ciste.commands :refer [add-command! command-names]]
            [jiksnu.modules.core.actions :as actions]
            jiksnu.modules.command.filters
            [jiksnu.actions.auth-actions :as actions.auth]
            [jiksnu.actions.site-actions :as actions.site]
            [jiksnu.actions.stream-actions :as actions.stream]))

;; This is where the module definition will be

(def module
  {:name "Commands"
   :deps [["net.kronkltd/jiksnu-core"]]})

(defn start []
  (add-command! "auth"            #'actions.auth/login)
  (add-command! "config"          #'actions.site/get-config)
  ;; (add-command! "connect"       #'http.actions/connect)
  ;; (add-command! "get-load"        #'actions.site/get-load)
  (add-command! "get-model"       #'actions/get-model)
  (add-command! "get-page"        #'actions/get-page)
  (add-command! "get-stats"       #'actions.site/get-stats)
  (add-command! "get-sub-page"    #'actions/get-sub-page)
  (add-command! "help"            #'command-names)
  (add-command! "invoke-action"   #'actions/invoke-action)
  (add-command! "list-activities" #'actions.stream/public-timeline)
  (add-command! "ping"            #'actions.site/ping)
  (add-command! "whoami"          #'actions.auth/whoami))

(defn stop [])
