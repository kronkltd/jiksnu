(ns jiksnu.actions.domain-actions-test
  (:use [ciste.config :only [with-environment]]
        [clj-factory.core :only [factory fseq]]
        [clj-tigase.core :only [deliver-packet!]]
        [jiksnu.test-helper :only [context test-environment-fixture]]
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

 (context "#'create"
   (context "when given valid options"
     (context "and the domain does not already exist"
       (model.domain/drop!)
       (let [options (prepare-create {:_id (fseq :domain)})]
         (create options) => model/domain?))
     ;; TODO: already exists
     )
   ;; TODO: invalid options
   )

 (context "#'delete"

   ;; There is no reason this shouldn't be a success
   (future-fact "when the domain does not exist"
     (model.domain/drop!)
     (let [domain (factory :domain {:_id (fseq :domain)})]
       (delete domain) => nil?))

   (context "when the domain exists"
     (let [domain (mock/a-domain-exists)]
       (delete domain) =>
       (every-checker
        #(= domain %)
        (fn [_] (nil? (model.domain/fetch-by-id (:_id domain))))))))

 (context "#'discover-onesocialweb"
   (context "when there is no url context"
     (context "should send a packet to that domain"
       (let [action #'discover
             domain (mock/a-domain-exists)
             url nil]
         (discover-onesocialweb domain url) => domain
         (provided
           (deliver-packet! anything) => nil :times 1))))
   (context "when there is a url context"
     (context "should send a packet to that domain"
       (let [action #'discover
             domain (mock/a-domain-exists)
             url (str "http://" (:_id domain) "/status/users/1")]
         (discover-onesocialweb domain url) => domain
         (provided
           (deliver-packet! anything) => nil :times 1)))))

 (context "#'discover-statusnet-config"
   (let [domain (mock/a-domain-exists)
         url (fseq :uri)
         res (l/result-channel)
         ch (l/channel)
         response  {:body "{\"foo\": \"bar\"}"}
         field-set (atom false)]

     (l/siphon (trace/probe-channel :domains:fieldSet) ch)
     (l/receive ch (fn [& args]
                     (dosync
                      (reset! field-set true))))

     (l/enqueue res response)
     (discover-statusnet-config domain url) => truthy

     (provided
      (model.domain/statusnet-url domain) => .url.
      (ops/update-resource .url.) => res)

     (l/close ch)
     @field-set => true))

 (context "#'discover-webfinger"
   (let [domain (mock/a-domain-exists)
         domain-name (:_id domain)]

     (context "when there is no url context"
       (let [url (factory/make-uri domain-name "/1")
             hm-url (factory/make-uri domain-name "/.well-known/host-meta")]
         (discover-webfinger domain url) => (contains {:_id domain-name})
         (provided
          (fetch-xrd* hm-url) => (cm/string->document "<XRD/>")
          (fetch-xrd* anything) => nil)))

     (context "when there is a url context"
       ;; TODO: Secure urls should always be checked first, and if the
       ;; provided url is secure, the non-secure urls should not be checked
       (let [url       (format "http://%s/status/users/1"                      domain-name)
             url-s     (format "https://%s/status/users/1"                     domain-name)
             hm-bare   (format "http://%s/.well-known/host-meta"               domain-name)
             hm-bare-s (format "https://%s/.well-known/host-meta"              domain-name)
             hm1       (format "http://%s/status/.well-known/host-meta"        domain-name)
             hm1-s     (format "https://%s/status/.well-known/host-meta"       domain-name)
             hm2       (format "http://%s/status/users/.well-known/host-meta"  domain-name)
             hm2-s     (format "https://%s/status/users/.well-known/host-meta" domain-name)]

         (context "and the bare domain has a host-meta"
           (discover-webfinger domain url) => (contains {:_id domain-name})
           (provided
            (fetch-xrd* hm-bare-s) => nil
            (fetch-xrd* hm-bare) => (cm/string->document "<XRD/>")))

         (context "and the bare domain does not have a host meta"

           (context "and none of the subpaths have host metas"
             (discover-webfinger domain url) => nil
             (provided

               (fetch-xrd* hm-bare) => nil
               (fetch-xrd* hm1)     => nil
               (fetch-xrd* hm2)     => nil
               (fetch-xrd* hm-bare-s) => nil))

           (context "and one of the subpaths has a host meta"
             ;; FIXME: this isn't being checked
             (discover-webfinger domain url) => (contains {:_id domain-name})
             (provided
               (fetch-xrd* hm-bare) => nil
               (fetch-xrd* hm-bare-s) => nil
               (fetch-xrd* hm1)     => nil
               (fetch-xrd* hm2)     => (cm/string->document "<XRD/>")))
           )
         ))
     ))

 (context "#'get-discovered"
   (let [domain (mock/a-domain-exists {:discovered false})]
     (get-discovered domain) => (contains {:discovered true})))

 (context "#'host-meta"
   (host-meta) =>
   (every-checker
    map?
    ;; TODO: verify the response map against the app's settings
    ))

 (context "#'show"
   (show .domain.) => .domain.)

 )
