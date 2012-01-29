(ns jiksnu.actions.domain-actions-test
  (:use (ciste [config :only [with-environment]])
        (clj-factory [core :only [factory fseq]])
        clojure.test
        (jiksnu model test-helper)
        jiksnu.actions.domain-actions
        midje.sweet)
  (:require (clj-tigase [packet :as packet])
            (jiksnu.model [domain :as model.domain]))
  (:import jiksnu.model.Domain))

(with-environment :test

  (test-environment-fixture)

  (fact "create"
    (fact "should create the domain"
    (let [options {:_id (fseq :domain)}]
      (create options) => domain?)))

  (fact "delete"
    (fact "when the domain does not exist"
      (fact "should return nil"
        (let [domain (factory Domain)]
          (delete domain) => nil?)))

    (future-fact "when the domain exists"
                 (against-background
                   [(around :facts
                            (let [domain (create (factory Domain))]
                              ?form))]
                   (fact "should return the deleted domain"
                     (delete domain) => domain)
                   
                   (fact "should delete the domain"
                     (delete domain)
                     (show domain) => nil?))))

 ;; (deftest test-discover-onesocialweb)

 (fact "should send a packet to that domain"
   (let [action #'discover
         domain (create (factory Domain))
         id (:_id domain)]
     (discover-onesocialweb domain) => packet/packet?))

 ;; (deftest test-host-meta)

 (fact "should return a XRD object"
   (host-meta) => map?))

