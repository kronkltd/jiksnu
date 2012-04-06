(ns jiksnu.actions.domain-actions-test
  (:use (ciste [config :only [with-environment]])
        (clj-factory [core :only [factory fseq]])
        (clj-tigase [core :only [deliver-packet!]])
        (jiksnu [test-helper :only [test-environment-fixture]])
        jiksnu.actions.domain-actions
        midje.sweet)
  (:require (ciste [model :as cm])
            (clj-tigase [packet :as packet])
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
   (fact "when there is no url context"
     (fact "should send a packet to that domain"
       (let [action #'discover
             domain (model.domain/create (factory Domain))
             url nil]
         (discover-onesocialweb domain url) => domain)
       (provided
         (deliver-packet! anything) => nil :times 1)))
   (fact "when there is a url context"
     (fact "should send a packet to that domain"
       (let [action #'discover
             domain (model.domain/create (factory Domain))
             url (str "http://" (:_id domain) "/status/users/1")]
         (discover-onesocialweb domain url) => domain)
       (provided
         (deliver-packet! anything) => nil :times 1))))

 (fact "#'path-segments"
   (path-segments "http://example.com/status/users/1") =>
   '("http://example.com/"
     "http://example.com/status/"
     "http://example.com/status/users/"))

 (fact "#'discover-webfinger"
   (fact "when there is no url context"
     (let [domain (model.domain/create (factory Domain))
           url nil]
       (discover-webfinger domain url) => (contains {:_id (:_id domain)})
       (provided
         (model.webfinger/fetch-host-meta anything) => (cm/string->document "<XRD/>"))))
   (fact "when there is a url context"
     (fact "and the bare domain has a host-meta"
       (let [domain (model.domain/create (factory Domain))
             url (str "http://" (:_id domain) "/status/users/1")]
         (discover-webfinger domain url) => (contains {:_id (:_id domain)})
         (provided
           (model.webfinger/fetch-host-meta anything) => (cm/string->document "<XRD/>"))))
     (fact "and the bare domain does not have a host meta"
       (fact "and none of the subpaths have host metas"
         (fact "should raise an exception"
           (let [domain (model.domain/create (factory Domain))
                 url (str "http://" (:_id domain) "/status/users/1")
                 hm-bare (str "http://" (:_id domain) "/.well-known/host-meta")
                 hm1 (str "http://" (:_id domain) "/status/.well-known/host-meta")]
             (discover-webfinger domain url) => (throws RuntimeException)
             (provided
               (model.webfinger/fetch-host-meta hm-bare) => nil
               (model.webfinger/fetch-host-meta hm1) => nil))))
       (fact "and one of the subpaths has a host meta"
         (fact "should update the host meta path"
           (let [domain (model.domain/create (factory Domain))
                 url (str "http://" (:_id domain) "/status/users/1")
                 hm-bare (str "http://" (:_id domain) "/.well-known/host-meta")
                 hm1 (str "http://" (:_id domain) "/status/.well-known/host-meta")]
             (discover-webfinger domain url) => (contains {:discovered true
                                                           :_id (:_id domain)})
             (provided
               (model.webfinger/fetch-host-meta hm-bare) => nil
               (model.webfinger/fetch-host-meta hm1) => (cm/string->document "<XRD/>"))))))))
 
 (fact "#'get-user-meta-url"
   (fact "when the domain doesn't exist"
     (fact "should return nil"
       (get-user-meta-url nil "acct:foo@example.com") => nil?)))
 
 (fact "host-meta"
   (fact "should return a XRD object"
     (host-meta) => map?)))

