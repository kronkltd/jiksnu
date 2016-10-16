(ns jiksnu.actions.service-actions-test
  (:require [ciste.model :as cm]
            [clj-factory.core :refer [factory fseq]]
            [clojure.data.json :as json]
            [jiksnu.actions.service-actions :as actions.service]
            [jiksnu.actions.resource-actions :as actions.resource]
            [jiksnu.factory :as factory]
            [jiksnu.mock :as mock]
            [jiksnu.model.domain :as model.domain]
            [jiksnu.test-helper :as th]
            [manifold.deferred :as d]
            [midje.sweet :refer :all])
  (:import jiksnu.model.Domain
           nu.xom.Document))

(th/module-test ["jiksnu.modules.core"])

(fact "#'actions.service/fetch-xrd"

  (let [domain (-> (mock/a-domain-exists))
        domain-name (:_id domain)
        hm-bare   (format "http://%s/.well-known/host-meta" domain-name)
        hm-bare-s (format "https://%s/.well-known/host-meta" domain-name)]

    (doto domain
      (model.domain/set-field! :http true)
      (model.domain/set-field! :https true))

    (let [domain (model.domain/fetch-by-id domain-name)]
      (fact "when the url is secure"
        (fact "when there is no url context"
          (let [url (format "https://%s/1" domain-name)]
            (actions.service/fetch-xrd domain url) => (partial instance? Document)
            (provided
             (actions.service/fetch-xrd* hm-bare-s) => (cm/string->document "<XRD/>"))))

        (fact "when there is a url context"
          ;; TODO: Secure urls should always be checked first, and if the
          ;; provided url is secure, the non-secure urls should not be checkedxs
          (let [url (format "http://%s/status/users/1" domain-name)
                url-s (format "https://%s/status/users/1" domain-name)
                hm1 (format "http://%s/status/.well-known/host-meta" domain-name)
                hm1-s (format "https://%s/status/.well-known/host-meta" domain-name)
                hm2 (format "http://%s/status/users/.well-known/host-meta" domain-name)
                hm2-s (format "https://%s/status/users/.well-known/host-meta" domain-name)]

            (fact "and the bare domain has a host-meta"
              (actions.service/fetch-xrd domain url-s) => (partial instance? Document)
              (provided
               (actions.service/fetch-xrd* hm-bare-s) => (cm/string->document "<XRD/>")))

            (fact "and the bare domain does not have a host meta"

              (fact "and none of the subpaths have host metas"
                (actions.service/fetch-xrd domain url-s) => nil
                (provided
                 ;; (actions.service/fetch-xrd* hm-bare) => nil
                 (actions.service/fetch-xrd* hm-bare-s) => nil
                 ;; (actions.service/fetch-xrd* hm1)     => nil
                 (actions.service/fetch-xrd* hm1-s)     => nil
                 ;; (actions.service/fetch-xrd* hm2)     => nil
                 (actions.service/fetch-xrd* hm2-s)     => nil))

              (fact "and one of the subpaths has a host meta"
                ;; FIXME: this isn't being checked
                (actions.service/fetch-xrd domain url-s) => (partial instance? Document)
                (provided
                 (actions.service/fetch-xrd* hm-bare-s) => nil
                 (actions.service/fetch-xrd* hm1-s)     => nil
                 (actions.service/fetch-xrd* hm2-s)     => (cm/string->document "<XRD/>"))))))))))

(fact "#'actions.service/discover-statusnet-config"
  (let [domain (mock/a-domain-exists)
        url (fseq :uri)
        statusnet-url (fseq :uri)
        res (d/deferred)
        config {:foo "bar"}
        response {:body (json/write-str config)}]

    (d/success! res response)
    (actions.service/discover-statusnet-config domain url) => truthy

    (provided
     (model.domain/statusnet-url domain) => statusnet-url
     (actions.resource/fetch statusnet-url) => res)

    (model.domain/fetch-by-id (:_id domain)) =>
    (contains {:statusnet-config config})))

(fact "#'actions.service/discover-capabilities"
  (fact "when given an invalid domain"
    (let [domain (mock/a-domain-exists)]
      (actions.service/discover-capabilities domain) =>
      (partial instance? Domain))))

;; TODO: If https is enabled, the bare path is checked at the https
;; path first
(fact "#'actions.service/discover-webfinger"
  (let [domain (mock/a-domain-exists)
        domain-name (:_id domain)]

    (fact "when the domain doesn't have a web interface"

      (model.domain/set-field! domain :http false)
      (model.domain/set-field! domain :https false)

      (let [url (factory/make-uri domain-name "/1")]
        (actions.service/discover-webfinger domain url) => nil
        (provided
         (actions.service/fetch-xrd domain url) => nil)))

    (fact "when there is no url context"
      (let [url (factory/make-uri domain-name "/1")
            hm-url (factory/make-uri domain-name "/.well-known/host-meta")]

        (actions.service/discover-webfinger domain url) => (contains {:_id domain-name})

        (provided
         (actions.service/fetch-xrd domain url) => (cm/string->document "<XRD/>"))))

    (fact "when there is a url context"
      ;; TODO: Secure urls should always be checked first, and if the
      ;; provided url is secure, the non-secure urls should not be checkedxs
      (let [url       (format "http://%s/status/users/1"                      domain-name)
            url-s     (format "https://%s/status/users/1"                     domain-name)
            hm-bare   (format "http://%s/.well-known/host-meta"               domain-name)
            hm-bare-s (format "https://%s/.well-known/host-meta"              domain-name)
            hm1       (format "http://%s/status/.well-known/host-meta"        domain-name)
            hm1-s     (format "https://%s/status/.well-known/host-meta"       domain-name)
            hm2       (format "http://%s/status/users/.well-known/host-meta"  domain-name)
            hm2-s     (format "https://%s/status/users/.well-known/host-meta" domain-name)]

        (fact "and the bare domain has a host-meta"
          (actions.service/discover-webfinger domain url) =>
          (contains {:_id domain-name})
          (provided
           ;; (actions.service/fetch-xrd* hm-bare-s) => nil
           (actions.service/fetch-xrd* hm-bare-s) =>
           (cm/string->document "<XRD/>")))

        (fact "and the bare domain does not have a host meta"

          (fact "and none of the subpaths have host metas"
            (actions.service/discover-webfinger domain url) => nil
            (provided

             (actions.service/fetch-xrd* hm-bare-s) => nil
             (actions.service/fetch-xrd* hm1-s)     => nil
             (actions.service/fetch-xrd* hm2-s)     => nil
             ;; (actions.service/fetch-xrd* hm-bare-s) => nil
             ))

          (fact "and one of the subpaths has a host meta"
            ;; FIXME: this isn't being checked
            (actions.service/discover-webfinger domain url) => (contains {:_id domain-name})
            (provided
             (actions.service/fetch-xrd* hm-bare-s) => nil
             ;; (actions.service/fetch-xrd* hm-bare-s) => nil
             (actions.service/fetch-xrd* hm1-s)     => nil
             (actions.service/fetch-xrd* hm2-s)     => (cm/string->document "<XRD/>"))))))))

(fact "#'actions.service/get-discovered"
  (let [domain (mock/a-domain-exists {:discovered false})]
    (actions.service/get-discovered domain) => (contains {:discovered true})))
