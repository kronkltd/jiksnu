(ns jiksnu.modules.command.routes.admin-routes
  (:require [ciste.commands :refer [add-command!]]
            [jiksnu.modules.admin.actions.worker-actions :as admin.worker]))

(add-command! "list-workers" #'admin.worker/index)
(add-command! "start-worker" #'admin.worker/start-worker)
(add-command! "stop-worker"  #'admin.worker/stop-worker)

