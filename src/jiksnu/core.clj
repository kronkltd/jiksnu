(ns jiksnu.core
  (:require [jiksnu.db :as db]
            jiksnu.modules.core.triggers.activity-triggers
            jiksnu.modules.core.triggers.conversation-triggers
            jiksnu.modules.core.triggers.domain-triggers
            jiksnu.workers))

(defn start
  []
  #_(timbre/info "starting core")
  (db/set-database!))
