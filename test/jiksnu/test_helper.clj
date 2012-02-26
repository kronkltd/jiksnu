(ns jiksnu.test-helper
  (:use (ciste [config :only [load-config with-environment]]
               [triggers :only [*thread-pool*]])
        midje.sweet
        (jiksnu [model :only [drop-all!]]))
  (:require (clojure.tools [logging :as log])
            (karras [entity :as entity])))

(load-config)

(defmacro test-environment-fixture
  [& body]
  `(do
     (log/info "****************************************************************************")
     (log/info (str "Testing " *ns*))
     (log/info "****************************************************************************")
     (log/info " ")
     (with-environment :test
       #_(background
        (around :contents
                (do
                  ?form
                  #_(.shutdown @*thread-pool*))))

       ~@body
       )))
