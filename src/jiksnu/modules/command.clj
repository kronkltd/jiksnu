(ns jiksnu.modules.command
  (:require [ciste.commands :refer [add-command! command-names]]
            [jiksnu.actions :as actions]
            jiksnu.modules.command.filters
            ;; [jiksnu.modules.http.actions :as http.actions]
))

;; This is where the module definition will be

(def module
  {:name "Commands"
   :deps [
          ["net.kronkltd/jiksnu-core"]
          ]
   }
  )

(add-command! "invoke-action" #'actions/invoke-action)
;; (add-command! "connect"       #'http.actions/connect)
(add-command! "get-model"     #'actions/get-model)
(add-command! "get-page"      #'actions/get-page)
(add-command! "get-sub-page"  #'actions/get-sub-page)

(add-command! "help" #'command-names)

