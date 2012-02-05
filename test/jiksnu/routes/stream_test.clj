(ns jiksnu.routes.stream-test
  (:use (ciste [config :only [with-environment]])
        midje.sweet
        lamina.core)
  (:require (jiksnu [routes :as r])
            (ring.mock [request :as mock])))

(with-environment :test
  (future-fact "index-http-route"
    (fact "when the serialization is :http"
      (fact "and there are no activities"
        (let [ch (channel)]
          (r/app ch (mock/request :get "/" ))
          (let [response (wait-for-message ch 5000)]
            response => (contains {:status 200}))))))


)
