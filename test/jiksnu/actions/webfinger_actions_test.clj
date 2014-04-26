(ns jiksnu.actions.webfinger-actions-test
  (:require [ciste.config :refer [config]]
            [clj-factory.core :refer [fseq]]
            [clojure.tools.logging :as log]
            [jiksnu.actions.webfinger-actions :refer [fetch-host-meta host-meta]]
            jiksnu.factory
            [jiksnu.mock :as mock]
            [jiksnu.model.user :as model.user]
            [jiksnu.model.webfinger :as model.webfinger]
            [jiksnu.test-helper :refer [check context future-context test-environment-fixture]]
            [midje.sweet :refer [=> truthy throws]]))

(test-environment-fixture

 (future-context #'fetch-host-meta
   (let [resource (mock/a-resource-exists)
         url (:url resource)]
     (context "when the url is nil"
       (fetch-host-meta nil) => (throws AssertionError))
     (context "when the url points to a valid XRD document"
       (fetch-host-meta url) => (partial instance? Document)
       (provided
         (ops/update-resource resource) => {:status 200
                                            :body "<XRD/>"}))
     (context "when the url does not point to a valid XRD document"
       (fetch-host-meta url) => (throws RuntimeException)
       (provided
         (ops/update-resource resource) => {:status 404
                                            :body "<html><body><p>Not Found</p></body></html>"}))))

 ;; (future-context #'fetch-user-meta
 ;;   (context "when the user has a user meta link"
 ;;     (context "when the user meta can be found"
 ;;       (context "should return a xml stream"
 ;;         (fetch-user-meta .user.) => truthy
 ;;         (provided
 ;;           (model.user/user-meta-uri .user.) => .url.
 ;;           (model.webfinger/fetch-host-meta .url.) => truthy)))
 ;;     (context "when the user meta can not be found"
 ;;       (context "should return an xml stream"
 ;;         (fetch-user-meta .user.) => nil
 ;;         (provided
 ;;           (model.user/user-meta-uri .user.) => .url.
 ;;           (model.webfinger/fetch-host-meta .url.) => nil))))
 ;;   (context "when the user does not have a user meta link"
 ;;     (context "should throw an exception"
 ;;       (fetch-user-meta .user.) => (throws RuntimeException)
 ;;       (provided
 ;;         (model.user/user-meta-uri .user.) => nil))))

 (context #'host-meta
   (let [domain (config :domain)]
     (host-meta) =>
     (check [response]
       response => map?
       (:host response) => domain
       (count (:links response)) => (partial >= 1))))
 )
