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
            [jiksnu.factory :as factory]
            [jiksnu.mock :as mock]
            [jiksnu.model :as model]
            [jiksnu.model.domain :as model.domain]
            [jiksnu.model.webfinger :as model.webfinger]
            [jiksnu.ops :as ops]
            [lamina.core :as l]
            [lamina.trace :as trace]))

(test-environment-fixture

 (fact "#'create"
   (fact "when given valid options"
     (fact "and the domain does not already exist"
       (model.domain/drop!)
       (let [options (prepare-create {:_id (fseq :domain)})]
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
     (let [domain (mock/a-domain-exists)]
       (delete domain) =>
       (every-checker
        #(= domain %)
        (fn [_] (nil? (model.domain/fetch-by-id (:_id domain))))))))

 (fact "#'discover-onesocialweb"
   (fact "when there is no url context"
     (fact "should send a packet to that domain"
       (let [action #'discover
             domain (mock/a-domain-exists)
             url nil]
         (discover-onesocialweb domain url) => domain
         (provided
           (deliver-packet! anything) => nil :times 1))))
   (fact "when there is a url context"
     (fact "should send a packet to that domain"
       (let [action #'discover
             domain (mock/a-domain-exists)
             url (str "http://" (:_id domain) "/status/users/1")]
         (discover-onesocialweb domain url) => domain
         (provided
           (deliver-packet! anything) => nil :times 1)))))

 (fact "#'discover-statusnet-config"
   (let [domain (mock/a-domain-exists)
         url (fseq :uri)
         res (l/result-channel)
         response  {:body "{\"foo\": \"bar\"}"}
         field-set (atom false)]
     (l/receive (trace/probe-channel :domain:setField)
                (fn [& args] args))
     (l/enqueue res response)
     (discover-statusnet-config domain url) => nil


     (provided
      (model.domain/statusnet-url domain) => .url.
      (ops/update-resource .url.) => res)))

 (fact "#'discover-webfinger"
   (let [domain (mock/a-domain-exists)
         id      (:_id domain)]

     (fact "when there is no url context"
       (let [url (factory/make-uri id "/1")
             hm-url (factory/make-uri id "/.well-known/host-meta")]
         (discover-webfinger domain url) => (contains {:_id id})
         (provided
           (fetch-xrd* hm-url) => (cm/string->document "<XRD/>"))))

     (fact "when there is a url context"
       (let [url     (format "http://%s/status/users/1"                     id)
             hm-bare (format "http://%s/.well-known/host-meta"              id)
             hm1     (format "http://%s/status/.well-known/host-meta"       id)
             hm2     (format "http://%s/status/users/.well-known/host-meta" id)]
         (fact "and the bare domain has a host-meta"
           (discover-webfinger domain url) => (contains {:_id id})
           (provided
             (fetch-xrd* hm-bare) => (cm/string->document "<XRD/>")))
         (fact "and the bare domain does not have a host meta"
           (fact "and none of the subpaths have host metas"
             (fact "should raise an exception"
               (discover-webfinger domain url) => (throws RuntimeException)
               (provided
                 (fetch-xrd* hm-bare) => nil
                 (fetch-xrd* hm1)     => nil
                 (fetch-xrd* hm2)     => nil)))
           (fact "and one of the subpaths has a host meta"
             (fact "should update the host meta path"
               ;; FIXME: this isn't being checked
               (discover-webfinger domain url) => (contains {:_id id})
               (provided
                 (fetch-xrd* hm-bare) => nil
                 (fetch-xrd* hm1)     => nil
                 (fetch-xrd* hm2)     => (cm/string->document "<XRD/>")))))))))

 (fact "#'get-discovered"
   (let [domain (mock/a-domain-exists {:discovered false})]
     (get-discovered domain) => (contains {:discovered true})))

 (fact "#'host-meta"
   (host-meta) =>
   (every-checker
    map?
    ;; TODO: verify the response map against the app's settings
    ))

 (fact "#'show"
   (show .domain.) => .domain.)

 )
