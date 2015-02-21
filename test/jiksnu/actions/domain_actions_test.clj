(ns jiksnu.actions.domain-actions-test
  (:require [ciste.model :as cm]
            [clj-factory.core :refer [factory fseq]]
            [clojure.tools.logging :as log]
            [jiksnu.actions.domain-actions :as actions.domain]
            [jiksnu.actions.resource-actions :as actions.resource]
            [jiksnu.factory :as factory]
            [jiksnu.mock :as mock]
            [jiksnu.model :as model]
            [jiksnu.model.domain :as model.domain]
            [jiksnu.ops :as ops]
            [jiksnu.test-helper :as th]
            [lamina.core :as l]
            [lamina.trace :as trace]
            [midje.sweet :refer :all])
  (:import jiksnu.model.Domain
           nu.xom.Document))

(namespace-state-changes
 [(before :contents (th/setup-testing))
  (after :contents (th/stop-testing))])

(fact "#'actions.domain/create"
  (fact "when given valid options"
    (fact "and the domain does not already exist"
      (model.domain/drop!)
      (let [options (actions.domain/prepare-create {:_id (fseq :domain)})]
        (actions.domain/create options) => model/domain?))
    ;; TODO: already exists
    )
  ;; TODO: invalid options
  )

(fact "#'actions.domain/fetch-xrd"

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
            (actions.domain/fetch-xrd domain url) => (partial instance? Document)
            (provided
              (actions.domain/fetch-xrd* hm-bare-s) => (cm/string->document "<XRD/>"))))

        (fact "when there is a url context"
          ;; TODO: Secure urls should always be checked first, and if the
          ;; provided url is secure, the non-secure urls should not be checkedxs
          (let [url       (format "http://%s/status/users/1"                      domain-name)
                url-s     (format "https://%s/status/users/1"                     domain-name)
                hm1       (format "http://%s/status/.well-known/host-meta"        domain-name)
                hm1-s     (format "https://%s/status/.well-known/host-meta"       domain-name)
                hm2       (format "http://%s/status/users/.well-known/host-meta"  domain-name)
                hm2-s     (format "https://%s/status/users/.well-known/host-meta" domain-name)]

            (fact "and the bare domain has a host-meta"
              (actions.domain/fetch-xrd domain url-s) => (partial instance? Document)
              (provided
                (actions.domain/fetch-xrd* hm-bare-s) => (cm/string->document "<XRD/>")))

            (fact "and the bare domain does not have a host meta"

              (fact "and none of the subpaths have host metas"
                (actions.domain/fetch-xrd domain url-s) => nil
                (provided
                  ;; (actions.domain/fetch-xrd* hm-bare) => nil
                  (actions.domain/fetch-xrd* hm-bare-s) => nil
                  ;; (actions.domain/fetch-xrd* hm1)     => nil
                  (actions.domain/fetch-xrd* hm1-s)     => nil
                  ;; (actions.domain/fetch-xrd* hm2)     => nil
                  (actions.domain/fetch-xrd* hm2-s)     => nil))

              (fact "and one of the subpaths has a host meta"
                ;; FIXME: this isn't being checked
                (actions.domain/fetch-xrd domain url-s) => (partial instance? Document)
                (provided
                  (actions.domain/fetch-xrd* hm-bare-s) => nil
                  (actions.domain/fetch-xrd* hm1-s)     => nil
                  (actions.domain/fetch-xrd* hm2-s)     => (cm/string->document "<XRD/>")))
              )
            ))))))

(fact "#'actions.domain/delete"

  ;; There is no reason this shouldn't be a success
  (future-fact "when the domain does not exist"
    (model.domain/drop!)
    (let [domain (factory :domain {:_id (fseq :domain)})]
      (actions.domain/delete domain) => nil?))

  (fact "when the domain exists"
    (let [domain (mock/a-domain-exists)]
      (actions.domain/delete domain) => domain
      (model.domain/fetch-by-id (:_id domain)) => nil?)))

(fact "#'actions.domain/discover-statusnet-config"
  (let [domain (mock/a-domain-exists)
        url (fseq :uri)
        res (l/result-channel)
        ch (l/channel)
        response  {:body "{\"foo\": \"bar\"}"}
        field-set (atom false)]


    ;; (l/siphon (trace/probe-channel :domains:field:set) ch)
    ;; (l/receive ch (fn [& args]
    ;;                 (dosync
    ;;                  (reset! field-set true))))

    (l/enqueue res response)
    (actions.domain/discover-statusnet-config domain url) => truthy

    (provided
      (model.domain/statusnet-url domain) => .url.
      (ops/update-resource .url.) => res)

    (l/close ch)
    @field-set => true))

(fact "#'actions.domain/discover-capabilities"
  (fact "when given an invalid domain"
    (let [domain (mock/a-domain-exists)]
      (actions.domain/discover-capabilities domain) =>
      (partial instance? Domain))))

;; TODO: If https is enabled, the bare path is checked at the https
;; path first
(fact "#'actions.domain/discover-webfinger"
  (let [domain (mock/a-domain-exists)
        domain-name (:_id domain)]

    (fact "when the domain doesn't have a web interface"

      (model.domain/set-field! domain :http false)
      (model.domain/set-field! domain :https false)

      (let [url (factory/make-uri domain-name "/1")]
        (actions.domain/discover-webfinger domain url) => nil))

    (fact "when there is no url context"
      (let [url (factory/make-uri domain-name "/1")
            hm-url (factory/make-uri domain-name "/.well-known/host-meta")]

        (actions.domain/discover-webfinger domain url) => (contains {:_id domain-name})

        (provided
          (actions.domain/fetch-xrd domain url) => (cm/string->document "<XRD/>")
          )))

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
          (actions.domain/discover-webfinger domain url) => (contains {:_id domain-name})
          (provided
            ;; (actions.domain/fetch-xrd* hm-bare-s) => nil
            (actions.domain/fetch-xrd* hm-bare-s) => (cm/string->document "<XRD/>")))

        (fact "and the bare domain does not have a host meta"

          (fact "and none of the subpaths have host metas"
            (actions.domain/discover-webfinger domain url) => nil
            (provided

              (actions.domain/fetch-xrd* hm-bare-s) => nil
              (actions.domain/fetch-xrd* hm1-s)     => nil
              (actions.domain/fetch-xrd* hm2-s)     => nil
              ;; (actions.domain/fetch-xrd* hm-bare-s) => nil
              ))

          (fact "and one of the subpaths has a host meta"
            ;; FIXME: this isn't being checked
            (actions.domain/discover-webfinger domain url) => (contains {:_id domain-name})
            (provided
              (actions.domain/fetch-xrd* hm-bare-s) => nil
              ;; (actions.domain/fetch-xrd* hm-bare-s) => nil
              (actions.domain/fetch-xrd* hm1-s)     => nil
              (actions.domain/fetch-xrd* hm2-s)     => (cm/string->document "<XRD/>")
              ))
          )
        ))
    ))

(fact "#'actions.domain/get-discovered"
  (let [domain (mock/a-domain-exists {:discovered false})]
    (actions.domain/get-discovered domain) => (contains {:discovered true})))

(fact "#'actions.domain/show"
  (actions.domain/show .domain.) => .domain.)


