(ns jiksnu.modules.core.actions.webfinger-actions-test
  (:require [ciste.config :refer [config]]
            [jiksnu.modules.core.actions.webfinger-actions :as actions.webfinger]
            [jiksnu.mock :as mock]
            [jiksnu.modules.core.ops :as ops]
            [jiksnu.test-helper :as th]
            [midje.sweet :refer :all])
  (:import nu.xom.Document
           (org.apache.http HttpStatus)))

(th/module-test ["jiksnu.modules.core"])

(fact "#'actions.webfinger/fetch-host-meta"
  (let [resource (mock/a-resource-exists)
        url (:_id resource)]
    (fact "when the url is nil"
      (actions.webfinger/fetch-host-meta nil) => (throws AssertionError))
    (fact "when the url points to a valid XRD document"
      (actions.webfinger/fetch-host-meta url) => (partial instance? Document)
      (provided
       (ops/update-resource url) => (ref {:status HttpStatus/SC_OK
                                          :body   "<XRD/>"})))
    (fact "when the url does not point to a valid XRD document"
      (actions.webfinger/fetch-host-meta url) => (throws RuntimeException)
      (provided
       (ops/update-resource url) => (ref {:status HttpStatus/SC_NOT_FOUND
                                          :body "<html><body><p>Not Found</p></body></html>"})))))

(fact "#'actions.webfinger/host-meta"
  (let [domain (config :domain)
        response (actions.webfinger/host-meta)]
    response => map?
    (:host response) => domain
    (count (:links response)) => (partial <= 1)))
