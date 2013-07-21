(ns jiksnu.routes.user-routes-test
  (:use [clj-factory.core :only [factory]]
        [clojurewerkz.route-one.core :only [named-path]]
        [jiksnu.routes-helper :only [response-for]]
        [jiksnu.test-helper :only [check context future-context test-environment-fixture]]
        [midje.sweet :only [=>]]
        [slingshot.slingshot :only [try+]])
  (:require [clojure.data.json :as json]
            [clojure.tools.logging :as log]
            [clojure.tools.reader.edn :as edn]
            [clojurewerkz.support.http.statuses :as status]
            [jiksnu.actions.stream-actions :as actions.stream]
            [jiksnu.mock :as mock]
            [jiksnu.util :as util]
            [lamina.core :as l]
            [ring.mock.request :as req]))

(test-environment-fixture

 (context "index page"
   (->> (named-path "index users")
        (req/request :get)
        response-for) =>
        (check [response]
          response => map?
          (:status response) => status/success?
          (:body response) => string?))

 (context "registration page"
   (->> (named-path "register page")
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
                           :args [type (util/make-id)]}]
              (actions.stream/handle-message request) =>
              (check [response]
                (get response :action) => "error")))

          (context "when the record is found"
            (let [user (mock/a-user-exists)
                  request {:channel ch
                           :name command
                           :args [type (:_id user)]}]
              (actions.stream/handle-message request) =>
              (check [response]
                (get response :action) => "model-updated")))
          ))
       ))
   )
 )
