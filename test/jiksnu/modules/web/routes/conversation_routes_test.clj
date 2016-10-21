(ns jiksnu.modules.web.routes.conversation-routes-test
  (:require [clojure.data.json :as json]
            [jiksnu.mock :as mock]
            [jiksnu.routes-helper :refer [as-user response-for]]
            [jiksnu.test-helper :as th]
            [midje.sweet :refer :all]
            [ring.mock.request :as req])
  (:import (org.apache.http HttpStatus)))

(th/module-test ["jiksnu.modules.core"
                 "jiksnu.modules.web"])

(facts "route: conversation-api/collection :get"
  (let [conversation (mock/a-conversation-exists)
        path "/model/conversations"
        request (req/request :get path)
        response (response-for request)]
    response => (contains {:status HttpStatus/SC_OK})
    (let [body (json/read-str (:body response) :key-fn keyword)]
      body => (contains {:totalItems 1
                         :items (contains (str (:_id conversation)))}))))

(facts "route: converation-api/activities-stream :get"
  (let [conversation (mock/a-conversation-exists)
        url (str "/model/conversations/" (:_id conversation) "/activities")
        request (req/request :get url)
        activity (mock/an-activity-exists {:conversation conversation})]
    (let [response (response-for request)]
      response => (contains {:status HttpStatus/SC_OK})
      (let [body (some-> response :body (json/read-str :key-fn keyword))]
        body => (contains {:items (contains (str (:_id activity)))})))))
