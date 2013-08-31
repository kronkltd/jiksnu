(ns jiksnu.modules.command.routes.stream-routes
  (:require [ciste.commands :refer [add-command!]]
            [jiksnu.actions.stream-actions :as actions.stream]))

(add-command! "list-activities" #'actions.stream/public-timeline)

