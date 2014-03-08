(ns jiksnu.actions.domain-actions-test
  (:require [ciste.model :as cm]
            [clj-factory.core :refer [factory fseq]]
            [clj-tigase.core :refer [deliver-packet!]]
            [clj-tigase.packet :as packet]
            [clojure.tools.logging :as log]
            [jiksnu.actions.domain-actions :as actions.domain]
            [jiksnu.actions.resource-actions :as actions.resource]
            [jiksnu.factory :as factory]
            [jiksnu.mock :as mock]
            [jiksnu.model :as model]
            [jiksnu.model.domain :as model.domain]
            [jiksnu.ops :as ops]
            [jiksnu.test-helper :refer [check context future-context test-environment-fixture]]
            [lamina.core :as l]
            [lamina.trace :as trace]
            [midje.sweet :refer [=> anything contains truthy]])
  (:import jiksnu.model.Domain))

(test-environment-fixture

 (context #'actions.domain/create
   (context "when given valid options"
     (context "and the domain does not already exist"
       (model.domain/drop!)
       (let [options (actions.domain/prepare-create {:_id (fseq :domain)})]
         (actions.domain/create options) => model/domain?))
     ;; TODO: already exists
     )
   ;; TODO: invalid options
   )

 (context #'actions.domain/delete

   ;; There is no reason this shouldn't be a success
   (future-context "when the domain does not exist"
     (model.domain/drop!)
     (let [domain (factory :domain {:_id (fseq :domain)})]
       (actions.domain/delete domain) => nil?))

   (context "when the domain exists"
     (let [domain (mock/a-domain-exists)]
       (actions.domain/delete domain) =>
       (check [response]
         response => domain
         (model.domain/fetch-by-id (:_id domain)) => nil?))))

 (context #'actions.domain/discover-onesocialweb
   (context "when there is no url context"
     (context "should send a packet to that domain"
       (let [domain (mock/a-domain-exists)
             url nil]
         (actions.domain/discover-onesocialweb domain url) => domain
         (provided
           (deliver-packet! anything) => nil :times 1))))
   (context "when there is a url context"
     (context "should send a packet to that domain"
       (let [domain (mock/a-domain-exists)
             url (str "http://" (:_id domain) "/status/users/1")]
         (actions.domain/discover-onesocialweb domain url) => domain
         (provided
           (deliver-packet! anything) => nil :times 1)))))

 (context #'actions.domain/discover-statusnet-config
   (let [domain (mock/a-domain-exists)
         url (fseq :uri)
         res (l/result-channel)
         ch (l/channel)
         response  {:body "{\"foo\": \"bar\"}"}
         field-set (atom false)]

     (l/siphon (trace/probe-channel :domains:field:set) ch)
     (l/receive ch (fn [& args]
                     (dosync
                      (reset! field-set true))))

     (l/enqueue res response)
     (actions.domain/discover-statusnet-config domain url) => truthy

     (provided
      (model.domain/statusnet-url domain) => .url.
      (ops/update-resource .url.) => res)

     (l/close ch)
     @field-set => true))

 (context #'actions.domain/discover-capabilities
   (context "when given an invalid domain"
     (let [domain (mock/a-domain-exists)]
       (actions.domain/discover-capabilities domain) =>
       (check [response]
         response => (partial instance? Domain)))))


 ;; TODO: If https is enabled, the bare path is checked at the https
 ;; path first
 (context #'actions.domain/discover-webfinger
   (let [domain (mock/a-domain-exists)
         domain-name (:_id domain)]

     (context "when there is no url context"
       (let [url (factory/make-uri domain-name "/1")
             hm-url (factory/make-uri domain-name "/.well-known/host-meta")]
         (actions.domain/discover-webfinger domain url) => (contains {:_id domain-name})
         (provided
          (actions.domain/fetch-xrd* hm-url) => (cm/string->document "<XRD/>"))))

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
           (actions.domain/discover-webfinger domain url) => (contains {:_id domain-name})
           (provided
            ;; (actions.domain/fetch-xrd* hm-bare-s) => nil
            (actions.domain/fetch-xrd* hm-bare) => (cm/string->document "<XRD/>")))

         (context "and the bare domain does not have a host meta"

           (context "and none of the subpaths have host metas"
             (actions.domain/discover-webfinger domain url) => nil
             (provided

               (actions.domain/fetch-xrd* hm-bare) => nil
               (actions.domain/fetch-xrd* hm1)     => nil
               (actions.domain/fetch-xrd* hm2)     => nil
               ;; (actions.domain/fetch-xrd* hm-bare-s) => nil
               ))

           (context "and one of the subpaths has a host meta"
             ;; FIXME: this isn't being checked
             (actions.domain/discover-webfinger domain url) => (contains {:_id domain-name})
             (provided
               (actions.domain/fetch-xrd* hm-bare) => nil
               ;; (actions.domain/fetch-xrd* hm-bare-s) => nil
               (actions.domain/fetch-xrd* hm1)     => nil
               (actions.domain/fetch-xrd* hm2)     => (cm/string->document "<XRD/>")
               ))
           )
         ))
     ))

 (context #'actions.domain/get-discovered
   (let [domain (mock/a-domain-exists {:discovered false})]
     (actions.domain/get-discovered domain) => (contains {:discovered true})))

 (context #'actions.domain/show
   (actions.domain/show .domain.) => .domain.)

 )
