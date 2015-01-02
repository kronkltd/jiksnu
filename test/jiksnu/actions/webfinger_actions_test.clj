(ns jiksnu.actions.webfinger-actions-test
  (:require [ciste.config :refer [config]]
            [clj-factory.core :refer [fseq]]
            [clojure.tools.logging :as log]
            [jiksnu.actions.webfinger-actions :refer [fetch-host-meta host-meta]]
            jiksnu.factory
            [jiksnu.mock :as mock]
            [jiksnu.model.user :as model.user]
            [jiksnu.model.webfinger :as model.webfinger]
            [jiksnu.test-helper :refer [check test-environment-fixture]]
            [midje.sweet :refer [=> fact future-fact truthy throws]]))

(test-environment-fixture

 (future-fact #'fetch-host-meta
   (let [resource (mock/a-resource-exists)
         url (:url resource)]
     (fact "when the url is nil"
       (fetch-host-meta nil) => (throws AssertionError))
     (fact "when the url points to a valid XRD document"
       (fetch-host-meta url) => (partial instance? Document)
       (provided
         (ops/update-resource resource) => {:status 200
                                            :body "<XRD/>"}))
     (fact "when the url does not point to a valid XRD document"
       (fetch-host-meta url) => (throws RuntimeException)
       (provided
         (ops/update-resource resource) => {:status 404
                                            :body "<html><body><p>Not Found</p></body></html>"}))))

 (future-fact #'fetch-user-meta
   (fact "when the user has a user meta link"
     (fact "when the user meta can be found"
       (fact "should return a xml stream"
         (fetch-user-meta .user.) => truthy
         (provided
           (model.user/user-meta-uri .user.) => .url.
           (model.webfinger/fetch-host-meta .url.) => truthy)))
     (fact "when the user meta can not be found"
       (fact "should return an xml stream"
         (fetch-user-meta .user.) => nil
         (provided
           (model.user/user-meta-uri .user.) => .url.
           (model.webfinger/fetch-host-meta .url.) => nil))))
   (fact "when the user does not have a user meta link"
     (fact "should throw an exception"
       (fetch-user-meta .user.) => (throws RuntimeException)
       (provided
         (model.user/user-meta-uri .user.) => nil))))

 (fact #'host-meta
   (let [domain (config :domain)
         response (host-meta)]
     (fact "returns a map"
       response => map?)

     (fact "host matches domain"
       (:host response) => domain)

     (fact "has links"
       (count (:links response)) => (partial >= 1))))
 )
