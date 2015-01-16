(ns jiksnu.core
  (:require [clojure.tools.logging :as log]
            [jiksnu.db :as db]
            ;; jiksnu.factory
            ;; jiksnu.formats
            jiksnu.modules.core.triggers.conversation-triggers
            jiksnu.modules.core.triggers.domain-triggers
            jiksnu.workers))

(defn start
  []
  (log/info "starting core")
  (db/set-database!)
  )

