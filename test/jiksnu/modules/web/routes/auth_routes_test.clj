(ns jiksnu.modules.web.routes.auth-routes-test
  (:require [clj-factory.core :refer [fseq]]
            [clojure.data.json :as json]
            [jiksnu.db :as db]
            [jiksnu.mock :as mock]
            jiksnu.modules.web.routes.auth-routes
            [jiksnu.routes-helper :refer [parse-cookie response-for]]
            [jiksnu.test-helper :as th]
            [jiksnu.util :as util]
            [midje.sweet :refer :all]
            [ring.mock.request :as req])
  (:import org.apache.http.HttpStatus))

(th/module-test ["jiksnu.modules.core"
                 "jiksnu.modules.web"])

(defn add-form-params
  [request params]
  (-> request
      (req/body (util/params-encode params))
      (req/content-type "application/x-www-form-urlencoded")))

(fact "route: auth/login :post"
  (fact "when given form parameters"
    (fact "when the params are correct"
      (db/drop-all!)
      (let [user (mock/a-user-exists)
            params {:username (:username user)
                    :password @mock/my-password}
            request (-> (req/request :post "/main/login")
                        (req/body params))
            response (response-for request)
            cookie (parse-cookie response)]

        response => (contains
                     {:status HttpStatus/SC_NO_CONTENT
                      :headers (contains {"Set-Cookie" truthy})})

        (some-> (req/request :get "/status")
                (assoc-in [:headers "cookie"] cookie)
                response-for :body
                (json/read-str :key-fn keyword) :user) => (:username user)))

    (fact "when the params are incorrect"
      (db/drop-all!)
      (let [user (mock/a-user-exists)
            params {:username (:username user)
                    :password (str @mock/my-password "_")}
            request (-> (req/request :post "/main/login")
                        (req/body params))
            response (response-for request)
            cookie (parse-cookie response)]

        response => (contains
                     {:status HttpStatus/SC_UNAUTHORIZED
                      :headers (contains {"Set-Cookie" truthy})})

        (some-> (req/request :get "/status")
                (assoc-in [:headers "cookie"] cookie)
                response-for :body
                (json/read-str :key-fn keyword) :user) => nil)))

  (future-fact "when given json parameters"
    (db/drop-all!)
    (let [user (mock/a-user-exists)
          params {:username (:username user)
                  :password @mock/my-password}
          request (-> (req/request :post "/main/login")
                      (req/body (json/write-str params))
                      (req/content-type "application/json"))
          response (response-for request)
          cookie (parse-cookie response)]
      response => (contains
                   {:status HttpStatus/SC_NO_CONTENT
                    :body string?
                    :headers (contains {"Set-Cookie" truthy})})

      (some-> (req/request :get "/status")
              (assoc-in [:headers "cookie"] cookie)
              response-for :body
              (json/read-str :key-fn keyword) :user) => (:username user))))

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
        (response-for request) => (contains {:status HttpStatus/SC_CREATED})))

    (fact "When a user with that username already exists"
      (db/drop-all!)
      (mock/a-user-exists {:username username})
      (let [request (add-form-params request params)]
        (response-for request) => (contains {:status HttpStatus/SC_CONFLICT})))))
