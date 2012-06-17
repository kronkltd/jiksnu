(ns jiksnu.routes.domain-test
  (use [jiksnu.test-helper :only [test-environment-fixture]]
       [jiksnu.routes :only [app]]
       midje.sweet
       [ring.mock.request :only [request]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.model :as model]
            [jiksnu.session :as session]
            [jiksnu.model.activity :as model.activity]
            [lamina.core :as l]))

(test-environment-fixture

 (fact "#'webfinger-host-meta"
   (fact "should return a XRD document"
     (let [ch (l/channel)]
       (app ch (request :get "/.well-known/host-meta"))
       (let [{:keys [body] :as response} (l/wait-for-message ch 5000)]
         response => (contains {:status 200})
         body => #"<XRD.*")))))
