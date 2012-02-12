(ns jiksnu.swank
  (:use (ciste [config :only [config]]))
  (:require (swank [swank :as swank])))


(defn start
  []
  (swank/start-repl (or (config :swank :port) "4005")))
