(ns jiksnu.routes.user-test
  (:use [clj-factory.core :only [factory]]
        [jiksnu.routes-helper :only [response-for]]
        [jiksnu.test-helper :only [test-environment-fixture]]
        [midje.sweet :only [every-checker fact future-fact => ]])
  (:require [clojure.tools.logging :as log]
            [clojurewerkz.support.http.statuses :as status]
            [ring.mock.request :as mock]))

(test-environment-fixture

 (fact "registration page"
   (->> "/main/register"
        (mock/request :get)
        response-for) =>
        (every-checker
         map?
         (comp status/success? :status)
         (comp string? :body)
         #(re-find #".*register-form.*" (:body %))))

 )
