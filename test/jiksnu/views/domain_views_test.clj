(ns jiksnu.views.domain-views-test
  (:use clj-factory.core
        (clj-tigase [element :only (element?)]
                    [packet :only (packet?)])
        clojure.test
        midje.sweet
        (jiksnu test-helper)
        jiksnu.views.domain-views)
  (:require (jiksnu.actions [domain-actions :as actions.domain]))
  (:import jiksnu.model.Domain))

(test-environment-fixture)
