(ns jiksnu.core-test
  (:use jiksnu.core
        jiksnu.model
        robert.hooke
        [lazytest.describe :only (describe do-it testing)])
  (:require [jiksnu.model.activity :as model.activity]
            [jiksnu.model.item :as model.item]
            [jiksnu.model.signature :as model.signature]
            [jiksnu.model.subscription :as model.subscription]
            [jiksnu.model.user :as model.user]))

(describe start)

(describe -main)

(defn env-hook
  [f & args]
  (with-environment :test
    ;; (model.activity/drop!)
    ;; (model.item/drop!)
    ;; (model.subscription/drop!)
    ;; (model.user/drop!)
    (apply f args)))

(add-hook #'lazytest.runner.console/run-tests env-hook)

