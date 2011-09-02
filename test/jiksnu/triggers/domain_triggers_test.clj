(ns jiksnu.triggers.domain-triggers-test
  (:use clojure.test
        midje.sweet
        jiksnu.triggers.domain-triggers)
  (:require (jiksnu.actions [domain-actions :as actions.domain])))

#_(deftest test-discover-onesocialweb
  (fact
    (discover-onesocialweb .action. [.domain.] .response.)
    (provided
      (apply-view {:fomat :xmpp
                   :serialization :xmpp
                   :action #'actions.domain/ping
                   } .domain.) => nil
      )
    )
  )

(deftest test-discover-webfinger)

(deftest test-create-trigger)
