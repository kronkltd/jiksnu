(ns jiksnu.actions.webfinger-actions-test
  (:require [ciste.config :refer [config]]
            [clj-factory.core :refer [fseq]]
            [clojure.tools.logging :as log]
            [jiksnu.actions.webfinger-actions :as actions.webfinger]
            jiksnu.factory
            [jiksnu.mock :as mock]
            [jiksnu.model.user :as model.user]
            [jiksnu.model.webfinger :as model.webfinger]
            [jiksnu.ops :as ops]
            [jiksnu.test-helper :as th]
            [midje.sweet :refer [=> after before fact future-fact
                                 namespace-state-changes truthy throws]]))

(namespace-state-changes
 [(before :contents (th/setup-testing))
  (after :contents (th/stop-testing))])

(future-fact "#'actions.webfinger/fetch-host-meta"
  (let [resource (mock/a-resource-exists)
        url (:url resource)]
    (fact "when the url is nil"
      (actions.webfinger/fetch-host-meta nil) => (throws AssertionError))
    (fact "when the url points to a valid XRD document"
      (actions.webfinger/fetch-host-meta url) => (partial instance? Document)
      (provided
        (ops/update-resource resource) => {:status 200
                                           :body "<XRD/>"}))
    (fact "when the url does not point to a valid XRD document"
      (actions.webfinger/fetch-host-meta url) => (throws RuntimeException)
      (provided
        (ops/update-resource resource) => {:status 404
                                           :body "<html><body><p>Not Found</p></body></html>"}))))

(future-fact "#'actions.webfinger/fetch-user-meta"
  (fact "when the user has a user meta link"
    (fact "when the user meta can be found"
      (actions.webfinger/fetch-user-meta .user.) => truthy
      (provided
        (model.user/user-meta-uri .user.) => .url.
        (model.webfinger/fetch-host-meta .url.) => truthy))
    (fact "when the user meta can not be found"
      (actions.webfinger/fetch-user-meta .user.) => nil
      (provided
        (model.user/user-meta-uri .user.) => .url.
        (model.webfinger/fetch-host-meta .url.) => nil)))
  (fact "when the user does not have a user meta link"
    (actions.webfinger/fetch-user-meta .user.) => (throws RuntimeException)
    (provided
      (model.user/user-meta-uri .user.) => nil)))

(fact "#'actions.webfinger/host-meta"
  (let [domain (config :domain)
        response (actions.webfinger/host-meta)]
    response => map?
    (:host response) => domain
    (count (:links response)) => (partial >= 1)))

