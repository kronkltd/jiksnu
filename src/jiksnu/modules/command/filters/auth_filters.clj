(ns jiksnu.modules.command.filters.auth-filters
  (:require [ciste.filters :only [deffilter]]
            [clojure.tools.logging :as log]
            [jiksnu.actions.auth-actions :as actions.auth]
            [jiksnu.model.user :as model.user]))

(deffilter #'actions.auth/login :command
  [action request]
  (let [[username password] (:args request)
        user (model.user/get-user username)]
    (action user password)))

;; whoami

(deffilter #'actions.auth/whoami :command
  [action request]
  (action))
