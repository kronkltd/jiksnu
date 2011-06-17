(ns jiksnu.core-test
  (:use jiksnu.core
        jiksnu.model
        [robert.hooke :only (add-hook)]
        [lazytest.describe :only (describe do-it testing)])
  (:require [jiksnu.model.activity :as model.activity]
            lazytest.runner.console
            lazytest.suite
            lazytest.test-case
            [jiksnu.model.item :as model.item]
            [jiksnu.model.signature :as model.signature]
            [jiksnu.model.subscription :as model.subscription]
            [jiksnu.model.user :as model.user]))

(describe start)

(describe -main)

(defn env-hook
  [f & args]
  (with-environment :test
    (apply f args)))

;; (with-environment :test
;;   (dosync
;;    (ref-set *mongo-database* (mongo-database*))))

(add-hook #'lazytest.test-case/try-test-case #'env-hook)

