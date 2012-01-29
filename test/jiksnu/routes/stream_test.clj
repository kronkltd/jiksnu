(ns jiksnu.routes.stream-test
  (:use midje.sweet
        lamina.core)
  (:require (jiksnu [routes :as r])
            (ring.mock [request :as mock])))

;; (deftest index-http-route-test)

(fact "when the serialization is :http"
  (fact "and there are no activities"
    (let [ch (channel)]
      (r/app ch (mock/request :get "/" ))
      (let [response (wait-for-message ch 5000)]
        response => (contains {:status 200})))))


