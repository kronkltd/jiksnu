(ns jiksnu.routes.user-routes-test
  (:use [clj-factory.core :only [factory]]
        [clojurewerkz.route-one.core :only [named-path]]
        [jiksnu.routes-helper :only [response-for]]
        [jiksnu.test-helper :only [check context future-context test-environment-fixture]]
        [midje.sweet :only [=>]])
  (:require [clojure.tools.logging :as log]
            [clojurewerkz.support.http.statuses :as status]
            [ring.mock.request :as req]))

(test-environment-fixture

 (context "index page"
   (->> (named-path "index users")
        (req/request :get)
        response-for) =>
        (check [response]
          response => map?
          (:status response) => status/success?
          (:body response) => string?))

 (context "registration page"
   (->> (named-path "register page")
        (req/request :get)
        response-for) =>
        (check [response]
          response => map?
          (:status response) => status/success?
          (:body response) => string?
          (:body response) => #".*register-form.*"))

 )
