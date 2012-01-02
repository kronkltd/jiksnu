(ns jiksnu.triggers.domain-triggers-test
  (:use (clj-factory [core :only (factory)])
        clojure.test
        midje.sweet
        (jiksnu test-helper)
        jiksnu.triggers.domain-triggers)
  (:require (jiksnu.actions [domain-actions :as actions.domain])
            (jiksnu.views [domain-views :as views.domain]))
  (:import jiksnu.model.Domain))

(test-environment-fixture)

;; (deftest test-create-trigger)
