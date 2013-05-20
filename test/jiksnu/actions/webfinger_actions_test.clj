(ns jiksnu.actions.webfinger-actions-test
  (:use [ciste.config :only [config]]
        [clj-factory.core :only [fseq]]
        [jiksnu.actions.webfinger-actions :only [host-meta]]
        [jiksnu.test-helper :only [test-environment-fixture]]
        [midje.sweet :only [fact future-fact => truthy every-checker throws]])
  (:require [clojure.tools.logging :as log]
            jiksnu.factory
            [jiksnu.mock :as mock]
            [jiksnu.model.user :as model.user]
            [jiksnu.model.webfinger :as model.webfinger]))

(test-environment-fixture
 (future-fact "#'fetch-host-meta"
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


 (future-fact "#'fetch-user-meta"
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

 (fact "#'host-meta"
   (let [domain (config :domain)]
     (host-meta) => (every-checker
                     map?
                     #(= domain (:host %))
                     #(>= 1 (count (:links %))))))

 )
