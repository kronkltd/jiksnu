(deftest index-http-route-test
  (testing "when the serialization is :http"
    (testing "and there are no activities"
      (let [ch (channel)]
        (r/app ch (mock/request :get "/" ))
        (let [response (wait-for-message ch 5000)]
          (is (= (:status response) 200)))))))


