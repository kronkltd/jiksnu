(ns jiksnu.routes.user-routes-test
  (:use [clj-factory.core :only [factory]]
        [clojurewerkz.route-one.core :only [named-path]]
        [jiksnu.routes-helper :only [response-for]]
        [jiksnu.test-helper :only [test-environment-fixture]]
        [midje.sweet :only [every-checker fact future-fact => ]])
  (:require [clojure.tools.logging :as log]
            [clojurewerkz.support.http.statuses :as status]
            [ring.mock.request :as req]))

(test-environment-fixture

 (fact "index page"
   (->> (named-path "index users")
        (req/request :get)
        response-for) =>
        (every-checker
         map?
         (comp status/success? :status)
         (comp string? :body)))

 (fact "registration page"
   (->> (named-path "register page")
        (req/request :get)
        response-for) =>
        (every-checker
         map?
         (comp status/success? :status)
         (comp string? :body)
         #(re-find #".*register-form.*" (:body %))))

 )
