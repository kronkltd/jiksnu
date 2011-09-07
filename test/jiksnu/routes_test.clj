(ns jiksnu.routes-test
  (use clj-factory.core
       clojure.test
       jiksnu.core-test
       [jiksnu.routes :only (app)]
       jiksnu.model
       jiksnu.model.activity
       jiksnu.session
       lamina.core
       ring.mock.request
       ))

(use-fixtures :once test-environment-fixture)

(deftest webfinger-host-meta-test
  (let [ch (channel)]
    (app ch (request :get "/.well-known/host-meta"))
    (let [response (wait-for-message ch 5000)
          {:keys [body]} response]
      (is (= (:status response) 200))
      (is (.startsWith body "<XRD")))))
