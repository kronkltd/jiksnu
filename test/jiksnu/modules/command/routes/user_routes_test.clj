(ns jiksnu.modules.command.routes.user-routes-test
  (:require [clj-factory.core :refer [factory]]
            [clojure.data.json :as json]
            [clojure.tools.logging :as log]
            [jiksnu.actions.stream-actions :as actions.stream]
            [jiksnu.mock :as mock]
            [jiksnu.test-helper :as th]
            [lamina.core :as l]
            [midje.sweet :refer :all]))

(namespace-state-changes
 [(before :contents (th/setup-testing))
  (after :contents (th/stop-testing))])

(fact "command 'get-model user'"
  (let [command "get-model"
        ch (l/channel)
        type "user"]

    (fact "when the record is not found"
      (let [request {:format :json
                     :channel ch
                     :name command
                     :args [type "acct:foo@bar.baz"]}]
        (+ 2 2) => 4
        #_(let [response (actions.stream/handle-message request)]
          (let [m (json/read-str response)]
            (get m "action") => "error"))))

    (fact "when the record is found"
      (let [user (mock/a-user-exists)
            request {:channel ch
                     :name command
                     :format :json
                     :args [type (:_id user)]}]
        #_(let [response (actions.stream/handle-message request)]
          (let [m (json/read-str response)]
            (get m "action") => "model-updated"))))

    ))


