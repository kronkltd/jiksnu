(ns jiksnu.modules.web.routes.user-routes-test
  (:require [clj-factory.core :refer [factory]]
            [clojure.data.json :as json]
            [clojure.tools.logging :as log]
            [clojure.tools.reader.edn :as edn]
            [clojurewerkz.support.http.statuses :as status]
            [jiksnu.actions.stream-actions :as actions.stream]
            [jiksnu.mock :as mock]
            jiksnu.modules.web.views.user-views
            [jiksnu.util :as util]
            [jiksnu.routes-helper :refer [response-for]]
            [jiksnu.test-helper :refer [check context future-context test-environment-fixture]]
            [lamina.core :as l]
            [midje.sweet :refer [=>]]
            [ring.mock.request :as req]
            [slingshot.slingshot :refer [try+]]))

(test-environment-fixture

 (context "index page"
   (->> "/users"
        (req/request :get)
        response-for) =>
        (check [response]
          response => map?
          (:status response) => status/success?
          (:body response) => string?))

 (context "registration page"
   (->> "/main/register"
        (req/request :get)
        response-for) =>
        (check [response]
          response => map?
          (:status response) => status/success?
          (:body response) => string?
          (:body response) => #".*register-form.*"))

 (context "commands"

   (context "get-model"
     (let [command "get-model"
           ch (l/channel)]

       (context "user"
         (let [type "user"]

           (context "when the record is not found"
             (let [request {:format :json
                            :channel ch
                            :name command
                            :args [type "acct:foo@bar.baz"]}]
               (actions.stream/handle-message request) =>
               (check [response]
                 (let [m (json/read-str response)]
                   (get m "action") => "error"))))

           (context "when the record is found"
             (let [user (mock/a-user-exists)
                   request {:channel ch
                            :name command
                            :format :json
                            :args [type (:_id user)]}]
               (actions.stream/handle-message request) =>
               (check [response]
                 (let [m (json/read-str response)]
                   (get m "action") => "model-updated"))))
           ))
       ))
   )
 )
