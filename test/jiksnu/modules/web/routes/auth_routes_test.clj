(ns jiksnu.modules.web.routes.auth-routes-test
  (:require [clj-factory.core :refer [fseq]]
            [clojure.string :as string]
            [taoensso.timbre :as log]
            [clojurewerkz.support.http.statuses :as status]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.db :as db]
            [jiksnu.mock :as mock]
            [jiksnu.routes-helper :refer [response-for]]
            [jiksnu.test-helper :as th]
            [midje.sweet :refer :all]
            [ring.mock.request :as req])
  (:import java.io.ByteArrayInputStream))

(namespace-state-changes
 [(before :contents (th/setup-testing))
  (after :contents (th/stop-testing))])

(defn add-form-params
  [request params]
  (let [body (->> params
                  (map (fn [[k v]] (str (name k) "=" v)))
                  (string/join "&"))]
    (-> request
        (req/body body)
        (req/content-type "application/x-www-form-urlencoded"))))

(future-fact "route: auth/login :post"
  (fact "when given correct parameters"
    (db/drop-all!)
    (let [username (fseq :username)
          password (fseq :password)
          user (actions.user/register {:username username
                                       :password password
                                       :accepted true})]
      (let [response (-> (req/request :post "/main/login")
                         (req/body {:username username :password password})
                         response-for)]
        response => map?
        (get-in response [:headers "Set-Cookie"]) => truthy
        (:status response) => status/redirect?
        (:body response) => string?))))

(fact "route: auth/register :post"
  (let [username (fseq :username)
        password (fseq :password)
        params {:username        username
                :password        password
                :confirmPassword password}
        request (req/request :post "/main/register")]

    (fact "With correct parameters, form encoded"
      (let [request (add-form-params request params)]
        (db/drop-all!)
        (response-for request) => (contains {:status 201})))

    (fact "When a user with that username already exists"
      (db/drop-all!)
      (mock/a-user-exists {:username username})
      (let [request (add-form-params request params)]
        (response-for request) => (contains {:status 409})))))
