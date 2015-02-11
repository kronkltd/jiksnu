(ns jiksnu.modules.web.routes.auth-routes-test
  (:require [aleph.formats :as formats]
            [clojure.tools.logging :as log]
            [clojurewerkz.support.http.statuses :as status]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.db :as db]
            [jiksnu.model :as model]
            [jiksnu.routes-helper :refer [response-for]]
            [jiksnu.test-helper :as th]
            [midje.sweet :refer :all]
            [ring.mock.request :as req]))

(namespace-state-changes
 [(before :contents (th/setup-testing))
  (after :contents (th/stop-testing))])

(future-fact "login"
  (fact "when given correct parameters"
    (db/drop-all!)
    (let [username (fseq :username)
          password (fseq :password)
          user (actions.user/register {:username username
                                       :password password
                                       :accepted true})]
      (let [response (-> (req/request :post "/main/login")
                         (assoc :content-type "application/x-www-form-urlencoded")
                         (assoc :body
                                (formats/bytes->input-stream
                                 (.getBytes
                                  (str "username=" username
                                       "&password=" password)
                                  "UTF-8")))
                         response-for)]
        response => map?
        (get-in response [:headers "Set-Cookie"]) => truthy
        (:status response) => status/redirect?
        (:body response) => string?))))


