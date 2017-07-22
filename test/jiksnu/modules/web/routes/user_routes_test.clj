(ns jiksnu.modules.web.routes.user-routes-test
  (:require [clojure.data.json :as json]
            [jiksnu.helpers.routes :refer [as-user response-for]]
            [jiksnu.modules.core.actions.group-actions :as actions.group]
            [jiksnu.mock :as mock]
            [jiksnu.test-helper :as th]
            [midje.sweet :refer :all]
            [ring.mock.request :as req]
            [net.cgrand.enlive-html :as enlive])
  (:import (org.apache.http HttpStatus)))

(th/module-test ["jiksnu.modules.core"
                 "jiksnu.modules.web"])

(fact "route: users-api/index :get"
  (let [url "/main/users"]
    (response-for (req/request :get url)) =>
    (contains {:status HttpStatus/SC_OK
               :body   string?})))

(fact "route: users-api/activities :get"
  (fact "When the user exists"
    (let [user (mock/a-user-exists)
          m 1]
      (dotimes [n m] (mock/an-activity-exists {:user user}))

      (let [path (format "/model/users/%s/activities" (:_id user))
            request (req/request :get path)
            response (response-for request)]
        response => (contains {:status HttpStatus/SC_OK})
        (let [parsed-body (some-> response :body (json/read-str :key-fn keyword))]
          parsed-body =>
          (contains {:items      (has every? string?)
                     :totalItems m}))))))

(fact "route: users-api/groups :get"
  (fact "When the user exists"
    (let [user (mock/a-user-exists)
          m 1]
      (dotimes [n m]
        (let [group (mock/a-group-exists)]
          (actions.group/add-user! group user)))

      (let [path (format "/model/users/%s/groups" (:_id user))
            request (req/request :get path)
            response (response-for request)]
        response => (contains {:status HttpStatus/SC_OK})
        (let [parsed-body (some-> response :body (json/read-str :key-fn keyword))]
          parsed-body =>
          (contains {:items (has every? string?)
                     :totalItems m}))))))

(future-fact "route: users-api/subscriptions :get"
  (let [user (mock/a-user-exists)
        subscription (mock/a-subscription-exists {:from user})
        path (str "/users/" (:_id user) "/subscriptions")]
    (-> (req/request :get path)
        response-for) =>
    (contains {:status HttpStatus/SC_OK
               :body   #(enlive/select (th/hiccup->doc %) [:.subscriptions])})))
