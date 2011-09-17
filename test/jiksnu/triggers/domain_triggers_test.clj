(ns jiksnu.triggers.domain-triggers-test
  (:use (clj-factory [core :only (factory)])
        clojure.test
        midje.sweet
        (jiksnu core-test)
        jiksnu.triggers.domain-triggers)
  (:require (clj-tigase [packet :as packet])
            (jiksnu.actions [domain-actions :as actions.domain])
            (jiksnu.views [domain-views :as views.domain]))
  (:import jiksnu.model.Domain))

(use-fixtures :each test-environment-fixture)

(deftest test-discover-onesocialweb
  (fact
    (let [action actions.domain/discover
          domain (actions.domain/create (factory Domain))
          response {}]
      (discover-onesocialweb action [domain] response) => packet/packet?)))

(deftest test-discover-webfinger)

(deftest test-create-trigger)
