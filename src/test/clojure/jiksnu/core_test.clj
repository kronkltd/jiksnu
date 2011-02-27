(ns jiksnu.core-test
  (:use jiksnu.core
        jiksnu.model
        robert.hooke
        [lazytest.describe :only (describe do-it testing)]))

(describe start)

(describe -main)

(defn env-hook
  [f & args]
  (with-environment :test
    (apply f args)))

(add-hook #'lazytest.test-case/try-test-case env-hook)

