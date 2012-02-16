(ns jiksnu.test-helper
  (:use (ciste [config :only [load-config with-environment]]
               [triggers :only [*thread-pool*]])
        midje.sweet
        (jiksnu [model :only [drop-all!]]))
  (:require (clojure.tools [logging :as log])
            jiksnu.factory
            (karras [entity :as entity])))

(defmacro test-environment-fixture
  [& body]
  `(do
     (println "****************************************************************************")
     (println (str "Testing " *ns*))
     (println "****************************************************************************")
     (println " ")
     (load-config)

     (with-environment :test
       #_(background
        (around :contents
                (do
                  ?form
                  #_(.shutdown @*thread-pool*))))

       ~@body
       )))
