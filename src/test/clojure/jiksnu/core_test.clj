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
  (println "env hook")
  (with-environment :test
    ;; (model.activity/drop!)
    ;; (model.item/drop!)
    ;; (model.subscription/drop!)
    ;; (model.user/drop!)
    (apply f args)))

(println "applying hook")
;; (add-hook #'lazytest.runner.console/run-tests env-hook)
;; (add-hook #'lazytest.suite/expand-suite env-hook)
(add-hook #'lazytest.test-case/try-test-case env-hook)

