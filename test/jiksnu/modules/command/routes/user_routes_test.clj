(ns jiksnu.modules.command.routes.user-routes-test
  (:require [clj-factory.core :refer [factory]]
            [clojure.data.json :as json]
            [clojure.tools.logging :as log]
            [clojure.tools.reader.edn :as edn]
            [jiksnu.actions.stream-actions :as actions.stream]
            [jiksnu.mock :as mock]
            jiksnu.modules.web.views.user-views
            [jiksnu.util :as util]
            [jiksnu.routes-helper :refer [response-for]]
            [jiksnu.test-helper :refer [test-environment-fixture]]
            [lamina.core :as l]
            [midje.sweet :refer [=> fact]]
            [slingshot.slingshot :refer [try+]]))

(test-environment-fixture

 (fact "command 'get-model user'"
   (let [command "get-model"
         ch (l/channel)
         type "user"]

     (fact "when the record is not found"
       (let [request {:format :json
                      :channel ch
                      :name command
                      :args [type "acct:foo@bar.baz"]}]
         (let [response (actions.stream/handle-message request)]
           (let [m (json/read-str response)]
             (get m "action") => "error"))))

     (fact "when the record is found"
       (let [user (mock/a-user-exists)
             request {:channel ch
                      :name command
                      :format :json
                      :args [type (:_id user)]}]
         (let [response (actions.stream/handle-message request)]
           (let [m (json/read-str response)]
             (get m "action") => "model-updated"))))

     ))

 )
