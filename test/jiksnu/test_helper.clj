(ns jiksnu.test-helper
  (:use [ciste.runner :only [load-site-config start-application!
                             stop-application! process-requires]]
        [slingshot.slingshot :only [try+]])
  (:require [clojure.tools.logging :as log]
            ;; jiksnu.factory
            [jiksnu.model :as model]))

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

     (model/drop-all!)
     
     ~@body
     (stop-application!)))
