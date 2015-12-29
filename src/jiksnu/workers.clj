(ns jiksnu.workers
  (:require [ciste.workers :refer [defworker stopping?]]
            [taoensso.timbre :as timbre]))

(defworker :gndn
  []
  (loop []
    (timbre/info "goes nowhere, does nothing")
    (Thread/sleep 10000)
    (if (not (stopping?))
      (recur))))

(defonce watchers (ref []))

(defworker :action-listener
  []
  (loop []
    (if (not (stopping?))
      (recur))))
