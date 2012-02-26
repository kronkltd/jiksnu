(ns jiksnu.routes.domain-test
  (use (ciste [config :only [with-environment]]
              [debug :only [spy]])
       (clojure [test :only [deftest]])
       (jiksnu [test-helper :only [test-environment-fixture]]
               [routes :only [app]])
       midje.sweet
       (ring.mock [request :only [request]]))
  (:require (jiksnu [model :as model]
                    [session :as session])
            (jiksnu.model [activity :as model.activity])
            (lamina [core :as l])))


(test-environment-fixture

  (fact "#'webfinger-host-meta"
    (fact "should return a XRD document"
      (let [ch (l/channel)]
        (app ch (request :get "/.well-known/host-meta"))
        (let [{:keys [body] :as response} (l/wait-for-message ch 5000)]
          response => (contains {:status 200})
          body => #"<XRD.*")))))
