(ns jiksnu.test-helper
  (:use [ciste.runner :only [load-site-config start-application!
                             stop-application! process-requires]])
  (:require [clojure.tools.logging :as log]
            jiksnu.factory))

(defmacro test-environment-fixture
  [& body]
  `(do
     (println "****************************************************************************")
     (println (str "Testing " *ns*))
     (println "****************************************************************************")
     (println " ")
     (load-site-config)
     (start-application! :test)
     (process-requires)
     ;; (Thread/sleep 6000)
     ~@body
     (stop-application!)))
