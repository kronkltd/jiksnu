(ns jiksnu.routes.auth-routes-test
  (:use [clj-factory.core :only [factory fseq]]
        [jiksnu.routes-helper :only [response-for]]
        [jiksnu.test-helper :only [test-environment-fixture]]
        [midje.sweet :only [every-checker fact future-fact => ]])
  (:require [aleph.formats :as formats]
            [clojure.tools.logging :as log]
            [clojurewerkz.support.http.statuses :as status]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.model :as model]
            [ring.mock.request :as mock]))

(test-environment-fixture

 (fact "login"
   (fact "when given correct parameters"
     (model/drop-all!)
     (let [username (fseq :username)
           password (fseq :password)
           user (actions.user/register {:username username
                                        :password password
                                        :accepted true})]
       (Thread/sleep 5000)
       (-> (mock/request :post "/main/login")
           (assoc :content-type "application/x-www-form-urlencoded")
           (assoc :body
             (formats/bytes->input-stream
              (.getBytes
               (str "username=" username
                    "&password=" password)
               "UTF-8")))
           response-for)) =>
           (every-checker
           map?
           (comp status/redirect? :status))))

 )
