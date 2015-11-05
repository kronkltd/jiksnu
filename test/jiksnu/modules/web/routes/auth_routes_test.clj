(ns jiksnu.modules.web.routes.auth-routes-test
  (:require [clj-factory.core :refer [fseq]]
            [clojure.string :as string]
            [clojurewerkz.support.http.statuses :as status]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.db :as db]
            [jiksnu.mock :as mock]
            jiksnu.modules.web.routes.auth-routes
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

(fact "route: auth/login :post"
  (fact "when given correct parameters"
    (db/drop-all!)
    (let [user (mock/a-user-exists)
          params {:username (:username user)
                  :password @mock/my-password}
          request (-> (req/request :post "/main/login")
                      (req/body params))]
      (response-for request) =>
      (contains
       {:status status/redirect?
        :body string?
        :headers (contains {"Set-Cookie" truthy})}))))

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
