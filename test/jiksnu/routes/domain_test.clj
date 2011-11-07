(ns jiksnu.routes.domain-test
  (use (ciste [debug :only [spy]])
       (clojure [test :only [deftest use-fixtures]])
       (jiksnu [core-test :only [test-environment-fixture]]
               [routes :only [app]])
       midje.sweet
       (ring.mock [request :only [request]]))
  (:require (jiksnu [model :as model]
                    [session :as session])
            (jiksnu.model [activity :as model.activity])
            (lamina [core :as l])))


(use-fixtures :once test-environment-fixture)

(deftest webfinger-host-meta-test
  (fact "should return a XRD document"
    (let [ch (l/channel)]
      (app ch (request :get "/.well-known/host-meta"))
      (let [{:keys [body] :as response} (l/wait-for-message ch 5000)]
        response => (contains {:status 200})
        body => #"<XRD.*"))))
