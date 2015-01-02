(ns jiksnu.modules.web.routes.auth-routes-test
  (:use [clj-factory.core :only [factory fseq]]
        [jiksnu.routes-helper :only [response-for]]
        [jiksnu.test-helper :only [check test-environment-fixture]]
        [midje.sweet :only [=> fact truthy]])
  (:require [aleph.formats :as formats]
            [clojure.tools.logging :as log]
            [clojurewerkz.support.http.statuses :as status]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.db :as db]
            [jiksnu.model :as model]
            [ring.mock.request :as req]))

(test-environment-fixture

 (fact "login"
   (fact "when given correct parameters"
     (db/drop-all!)
     (let [username (fseq :username)
           password (fseq :password)
           user (actions.user/register {:username username
                                        :password password
                                        :accepted true})]
       (-> (req/request :post "/main/login")
           (assoc :content-type "application/x-www-form-urlencoded")
           (assoc :body
             (formats/bytes->input-stream
              (.getBytes
               (str "username=" username
                    "&password=" password)
               "UTF-8")))
           response-for)) =>
           (check [response]
             response => map?
             (get-in response [:headers "Set-Cookie"]) => truthy
             (:status response) => status/redirect?
             (:body response) => string?)))

 )
