(ns jiksnu.actions.domain-actions-test
  (:use [ciste.config :only [with-environment]]
        [clj-factory.core :only [factory fseq]]
        [clj-tigase.core :only [deliver-packet!]]
        [jiksnu.test-helper :only [test-environment-fixture]]
        jiksnu.actions.domain-actions
        midje.sweet)
  (:require [ciste.model :as cm]
            [clj-tigase.packet :as packet]
            [clojure.tools.logging :as log]
            [jiksnu.actions.domain-actions :as actions.domain]
            [jiksnu.model :as model]
            [jiksnu.model.domain :as model.domain]
            [jiksnu.model.webfinger :as model.webfinger]))

(test-environment-fixture

 (fact "#'create"
   (fact "when given valid options"
     (fact "and the domain does not already exist"
       (model.domain/drop!)
       (let [options {:_id (fseq :domain)}]
         (create options) => model/domain?))
     ;; TODO: already exists
     )
   ;; TODO: invalid options
   )

 (fact "#'delete"

   ;; There is no reason this shouldn't be a success
   (future-fact "when the domain does not exist"
     (model.domain/drop!)
     (let [domain (factory :domain {:_id (fseq :domain)})]
       (delete domain) => nil?))

   (fact "when the domain exists"
     (let [domain (actions.domain/find-or-create (factory :domain))]
       (delete domain) =>
       (every-checker
        #(= domain %)
        (fn [_] (nil? (model.domain/fetch-by-id (:_id domain))))))))
 
 (fact "#'discover-onesocialweb"
   (fact "when there is no url context"
     (fact "should send a packet to that domain"
       (let [action #'discover
             domain (model.domain/create (factory :domain))
             url nil]
         (discover-onesocialweb domain url) => domain)
       (provided
         (deliver-packet! anything) => nil :times 1)))
   (fact "when there is a url context"
     (fact "should send a packet to that domain"
       (let [action #'discover
             domain (model.domain/create (factory :domain))
             url (str "http://" (:_id domain) "/status/users/1")]
         (discover-onesocialweb domain url) => domain)
       (provided
         (deliver-packet! anything) => nil :times 1))))

 (fact "#'discover-webfinger"
   (fact "when there is no url context"
     (let [domain (model.domain/create (factory :domain))
           url nil]
       (discover-webfinger domain url) => (contains {:_id (:_id domain)})
       (provided
         (model.webfinger/fetch-host-meta anything) => (cm/string->document "<XRD/>"))))
   (fact "when there is a url context"
     (fact "and the bare domain has a host-meta"
       (let [domain (model.domain/create (factory :domain))
             url (str "http://" (:_id domain) "/status/users/1")]
         (discover-webfinger domain url) => (contains {:_id (:_id domain)})
         (provided
           (model.webfinger/fetch-host-meta anything) => (cm/string->document "<XRD/>"))))
     (fact "and the bare domain does not have a host meta"
       (fact "and none of the subpaths have host metas"
         (fact "should raise an exception"
           (let [domain (model.domain/create (factory :domain))
                 url (str "http://" (:_id domain) "/status/users/1")
                 hm-bare (str "http://" (:_id domain) "/.well-known/host-meta")
                 hm1 (str "http://" (:_id domain) "/status/.well-known/host-meta")
                 hm2 (str "http://" (:_id domain) "/status/users/.well-known/host-meta")]
             (discover-webfinger domain url) => (throws RuntimeException)
             (provided
               (model.webfinger/fetch-host-meta hm-bare) => nil
               (model.webfinger/fetch-host-meta hm1) => nil
               (model.webfinger/fetch-host-meta hm2) => nil))))
       (fact "and one of the subpaths has a host meta"
         (fact "should update the host meta path"
           (let [domain (model.domain/create (factory :domain))
                 url (str "http://" (:_id domain) "/status/users/1")
                 hm-bare (str "http://" (:_id domain) "/.well-known/host-meta")
                 hm1 (str "http://" (:_id domain) "/status/.well-known/host-meta")
                 hm2 (str "http://" (:_id domain) "/status/users/.well-known/host-meta")]
             (discover-webfinger domain url) => (contains {:discovered true
                                                           :_id (:_id domain)})
             (provided
               (model.webfinger/fetch-host-meta hm-bare) => nil
               ;; (model.webfinger/fetch-host-meta hm2) => nil
               (model.webfinger/fetch-host-meta hm1) => (cm/string->document "<XRD/>"))))))))
 
 (fact "#'get-user-meta-url"
   (fact "when the domain doesn't exist"
     (fact "should return nil"
       (get-user-meta-url nil "acct:foo@example.com") => nil?)))
 
 (fact "#'host-meta"
   (host-meta) =>
   (every-checker
    map?
    ;; TODO: verify the response map against the app's settings
    ))

 (fact "#'show"
   (show .domain.) => .domain.)
 
)
