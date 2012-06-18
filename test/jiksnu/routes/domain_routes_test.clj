(ns jiksnu.routes.domain-routes-test
  (:use [jiksnu.routes-helper :only [response-for]]
        [jiksnu.test-helper :only [test-environment-fixture]]
        [midje.sweet :only [fact future-fact => every-checker contains]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.model :as model]
            [jiksnu.model.activity :as model.activity]
            [ring.mock.request :as mock]))

(test-environment-fixture

 (fact "#'webfinger-host-meta"
   (fact "should return a XRD document"
     (-> (mock/request :get "/.well-known/host-meta")
         response-for) =>
         (every-checker
          (contains {:status 200})
          (fn [req]
            (fact
              (:body req) => #"<XRD.*"))))))
