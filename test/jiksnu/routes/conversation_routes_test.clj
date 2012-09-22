(ns jiksnu.routes.conversation-routes-test
  (:use [clj-factory.core :only [factory]]
        [clojurewerkz.route-one.core :only [named-path]]
        [jiksnu.routes-helper :only [response-for]]
        [jiksnu.test-helper :only [test-environment-fixture]]
        [midje.sweet :only [every-checker fact future-fact => ]])
  (:require [clojure.tools.logging :as log]
            [clojurewerkz.support.http.statuses :as status]
            [ring.mock.request :as mock]))

(test-environment-fixture

 (fact "index page"
   (->> (named-path "index conversations")
        (mock/request :get)
        response-for) =>
        (every-checker
         map?
         (comp status/success? :status)
         (comp string? :body)))

  (fact "index page (:viewmodel)"
   (->> (str (named-path "index conversations") ".viewmodel")
        (mock/request :get)
        response-for) =>
        (every-checker
         map?
         (comp status/success? :status)
         (comp string? :body)))

 
 
 )
