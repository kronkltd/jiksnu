(ns jiksnu.modules.command.routes.stream-routes-test
  (:require [ciste.commands :refer [parse-command]]
            [clojure.data.json :as json]
            [clojure.tools.logging :as log]
            [jiksnu.test-helper :as th]
            [manifold.deferred :as d]
            [midje.sweet :refer :all]))

(namespace-state-changes
 [(before :facts (th/setup-testing))
  (after :facts (th/stop-testing))])

(future-fact "command 'get-page streams'"
  (let [name "get-page"
        args '("streams")
        ch (d/deferred)
        request {:name name
                 :channel ch
                 :format :json
                 :args args}
        response (parse-command request)]

    response => map?
    (let [body (:body response)]
      body => string?
      (let [response-obj (json/read-str body)]
        response-obj => map?))))


