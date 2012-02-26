(ns jiksnu.test-helper
  (:use (ciste [config :only [load-config with-environment]]
               [triggers :only [*thread-pool*]])
        midje.sweet
        (jiksnu [model :only [drop-all!]]))
  (:require (clojure.tools [logging :as log])
            (karras [entity :as entity])))

(load-config)

(defmacro test-environment-fixture
  []
  `(do
     (log/info (str "Testing " *ns*))
     (background
      (around :contents
              (do
                ?form
                (.shutdown @*thread-pool*))))))
