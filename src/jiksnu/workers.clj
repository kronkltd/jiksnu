(ns jiksnu.workers
  (:require [ciste.workers :refer [defworker stopping?]]
            [clojure.tools.logging :as log]
            [lamina.core :as l]))

(defworker :gndn
  []
  (loop []
    (log/info "goes nowhere, does nothing")
    (Thread/sleep 10000)
    (if (not (stopping?))
      (recur))))

(defonce watchers (ref []))

(defworker :action-listener
  []
  (loop []
    #_(when-let [message (l/wait-for-message *actions*)]
        (log/info (:action message)))
    (if (not (stopping?))
      (recur))))
