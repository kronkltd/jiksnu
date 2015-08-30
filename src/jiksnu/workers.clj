(ns jiksnu.workers
  (:require [ciste.workers :refer [defworker stopping?]]
            [clojure.tools.logging :as log]))

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
    (if (not (stopping?))
      (recur))))
