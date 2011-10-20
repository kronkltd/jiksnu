(ns jiksnu.routes.domain-test
  (use clojure.test
       jiksnu.core-test
       [jiksnu.routes :only (app)]
       lamina.core
       midje.sweet
       ring.mock.request)
  (:require (jiksnu [model :as model]
                    [session :as session])
            (jiksnu.model [activity :as model.activity])
            (lamina [core :as l])
            ))


(use-fixtures :once test-environment-fixture)

(deftest webfinger-host-meta-test
  (fact "should return a XRD document"
    (let [ch (channel)]
      (app ch (request :get "/.well-known/host-meta"))
      (let [{:keys [body] :as response} (l/wait-for-message ch 5000)]
        response => (contains {:status 200})
        body => #"<XRD.*"))))
