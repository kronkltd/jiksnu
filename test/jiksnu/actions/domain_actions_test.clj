(ns jiksnu.actions.domain-actions-test
  (:use (ciste [config :only [with-environment]])
        (clj-factory [core :only [factory fseq]])
        (jiksnu [test-helper :only [test-environment-fixture]])
        jiksnu.actions.domain-actions
        midje.sweet)
  (:require (clj-tigase [packet :as packet])
            (jiksnu [model :as model])
            (jiksnu.model [domain :as model.domain]
                          [webfinger :as model.webfinger]))
  (:import jiksnu.model.Domain))

(test-environment-fixture

 (fact "#'create"
   (fact "should create the domain"
     (let [options {:_id (fseq :domain)}]
       (create options) => model/domain?)))

 (fact "#'delete"
   (fact "when the domain does not exist"
     (fact "should return nil"
       (let [domain (factory Domain)]
         (delete domain) => nil?)))

   (future-fact "when the domain exists"
     (against-background
       [(around :facts
                (let [domain (model.domain/create (factory Domain))]
                  ?form))]
       (fact "should return the deleted domain"
         (delete domain) => domain)
       
       (fact "should delete the domain"
         (delete domain)
         (show domain) => nil?))))
 
 (fact "#'discover-onesocialweb"
   (fact "should send a packet to that domain"
     (let [action #'discover
           domain (model.domain/create (factory Domain))]
       (discover-onesocialweb domain) => packet/packet?)))

 (fact "#'discover-webfinger"
   (let [domain (model.domain/create (factory Domain))]
     (discover-webfinger domain) => (contains {:_id (:_id domain)} )
     (provided
       (model.webfinger/fetch-host-meta anything) =>
       (model/string->document "<XRD/>"))))

 (fact "#'get-user-meta-url"
   (fact "when the domain doesn't exist"
     (fact "should return nil"
       (get-user-meta-url nil "acct:foo@example.com") => nil?)))
 
 (fact "host-meta"
   (fact "should return a XRD object"
     (host-meta) => map?)))

