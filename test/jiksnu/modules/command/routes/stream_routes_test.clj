(ns jiksnu.modules.command.routes.stream-routes-test
  (:require [ciste.commands :refer [parse-command]]
            [clojure.data.json :as json]
            [clojure.tools.logging :as log]
            [jiksnu.test-helper :as th]
            [lamina.core :as l]
            [midje.sweet :refer [=> after before contains fact
                                 namespace-state-changes]]))

(namespace-state-changes
 [(before :contents (th/setup-testing))
  (after :contents (th/stop-testing))])

(fact "command 'get-page streams'"
  (let [name "get-page"
        args '("streams")
        ch (l/channel)
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


