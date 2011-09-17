(ns jiksnu.triggers.domain-triggers-test
  (:use (clj-factory [core :only (factory)])
        clojure.test
        midje.sweet
        (jiksnu core-test)
        jiksnu.triggers.domain-triggers)
  (:require (jiksnu.actions [domain-actions :as actions.domain])
            (jiksnu.views [domain-views :as views.domain]))
  (:import jiksnu.model.Domain))

(use-fixtures :once test-environment-fixture)

(deftest test-create-trigger)
