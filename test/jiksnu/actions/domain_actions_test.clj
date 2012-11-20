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
            [jiksnu.actions.resource-actions :as actions.resource]
            [jiksnu.existance-helpers :as existance]
            [jiksnu.factory :as factory]
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
   (let [domain (existance/a-domain-exists)
         id      (:_id domain)]
     (fact "when there is no url context"
       (let [url (factory/make-uri id "/1")
             hm-url (factory/make-uri id "/.well-known/host-meta")
             resource (existance/a-resource-exists {:url hm-url})]
         (discover-webfinger domain url) => (contains {:_id id})
         (provided
           (actions.resource/update* resource) => {:body "<XRD/>"})))
     (fact "when there is a url context"
       (let [url     (format "http://%s/status/users/1"                     id)
             hm-bare (format "http://%s/.well-known/host-meta"              id)
             hm1     (format "http://%s/status/.well-known/host-meta"       id)
             hm2     (format "http://%s/status/users/.well-known/host-meta" id)
             resource      (model/get-resource url)
             resource-bare (model/get-resource hm-bare)
             resource1     (model/get-resource hm1)
             resource2     (model/get-resource hm2)]
         (fact "and the bare domain has a host-meta"
           (discover-webfinger domain url) => (contains {:_id id})
           (provided
             (actions.resource/update* resource-bare) => {:body "<XRD/>"}))
         (fact "and the bare domain does not have a host meta"
           (fact "and none of the subpaths have host metas"
             (fact "should raise an exception"
               (discover-webfinger domain url) => (throws RuntimeException)
               (provided
                 (actions.resource/update* resource-bare) => {:body nil}
                 (actions.resource/update* resource1) => {:body nil}
                 (actions.resource/update* resource2) => {:body nil})))
           (fact "and one of the subpaths has a host meta"
             (fact "should update the host meta path"
               ;; FIXME: this isn't being checked
               (discover-webfinger domain url) => (contains {:_id id})
               (provided
                 (actions.resource/update* resource-bare) => {:body nil}
                 (actions.resource/update* resource1) => {:body nil}
                 (actions.resource/update* resource2) => {:body "<XRD/>"}))))))))

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
