(ns jiksnu.modules.command-test
  (:require [ciste.commands :refer [parse-command]]
            [ciste.core :refer [with-context]]
            [clj-factory.core :refer [factory]]
            [clojure.data.json :as json]
            [clojure.tools.logging :as log]
            [jiksnu.actions.stream-actions :as actions.stream]
            [jiksnu.mock :as mock]
            [jiksnu.modules.command]
            [jiksnu.test-helper :as th]
            [manifold.deferred :as d]
            [midje.sweet :refer :all]))

(namespace-state-changes
 [(before :facts (th/setup-testing))
  (after :facts (th/stop-testing))])

(fact "command 'get-page streams'"
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

(facts "command 'get-model user'"
  (let [command "get-model"
        ch (d/deferred)
        type "user"]

    (future-fact "when the record is not found"
      (let [request {:format :json
                     :channel ch
                     :name command
                     :args [type "acct:foo@bar.baz"]}]
        (let [response (parse-command request)]
          (let [m (json/read-str response)]
            (get m "action") => "error"))))

    (fact "when the record is found"
      (let [user (mock/a-user-exists)
            request {:channel ch
                     :name command
                     :format :json
                     :args (list "user" (:_id user))}
            response (parse-command request)
            m (json/read-str (:body response))]
        (get m "action") => "model-updated"))))

(future-fact "command 'get-page clients'"
  (let [name "get-page"
        args '("clients")]
    (fact "when there are clients"
      (let [client (mock/a-client-exists)]
        (let [ch (d/deferred)
              request {:channel ch
                       :name name
                       :format :json
                       :args args}]
          (let [response (parse-command request)]
            response => map?
            (let [body (:body response)]
              (let [json-obj (json/read-str body :key-fn keyword)]
                json-obj => map?))))))))
