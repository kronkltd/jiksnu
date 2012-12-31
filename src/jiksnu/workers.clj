(ns jiksnu.workers
  (:use [ciste.core :only [*actions*]]
        [ciste.workers :only [defworker stopping?]]
        lamina.core)
  (:require [clojure.tools.logging :as log]))

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
    (when-let [message (wait-for-message *actions*)]
      (log/info (:action message)))
    (if (not (stopping?))
      (recur))))
