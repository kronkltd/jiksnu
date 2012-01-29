(ns jiksnu.routes.stream-test)

;; (deftest index-http-route-test)

(fact "when the serialization is :http"
  (fact "and there are no activities"
    (let [ch (channel)]
      (r/app ch (mock/request :get "/" ))
      (let [response (wait-for-message ch 5000)]
        (is (= (:status response) 200))))))


