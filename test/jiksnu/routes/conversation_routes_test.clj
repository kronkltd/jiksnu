(ns jiksnu.routes.conversation-routes-test
  (:use [clj-factory.core :only [factory]]
        [clojurewerkz.route-one.core :only [named-path]]
        [jiksnu.routes-helper :only [response-for]]
        [jiksnu.test-helper :only [context test-environment-fixture]]
        [midje.sweet :only [=>]])
  (:require [clojure.tools.logging :as log]
            [clojurewerkz.support.http.statuses :as status]
            [ring.mock.request :as req]))

(test-environment-fixture

 (context "index page"
   (->> (named-path "index conversations")
        (req/request :get)
        response-for) =>
        (check [response]
          response => map?
          (:status response) => status/redirect?
          (:body response) => string?
          ))

 (context "index page (:viewmodel)"
   (->> (str (named-path "index conversations") ".viewmodel")
        (req/request :get)
        response-for) =>
        (check [response]
          response => map?
          (:status response) => status/redirect?
          (:body response) => string?))

 )
