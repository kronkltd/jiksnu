(ns jiksnu.modules.web.routes.site-routes-test
  (:require [ciste.formats :refer [format-as]]
            [ciste.model :as cm]
            [clojurewerkz.support.http.statuses :as status]
            [jiksnu.test-helper :as th]
            [jiksnu.routes-helper :refer [as-user response-for]]
            [midje.sweet :refer :all]
            [ring.mock.request :as req]))

(namespace-state-changes
 [(before :contents (th/setup-testing))
  (after :contents (th/stop-testing))])

(future-fact "rsd document"
  (let [response (-> (req/request :get "/rsd.xml") response-for)]
    response => map?
    (:status response) => status/success?
    (let [body (cm/string->document (:body response))
          root (.getRootElement body)
          attr {"rsd" "http://archipelago.phrasewise.com/rsd"}
          nodes (cm/query root "//rsd:rsd" attr)]
      (count nodes) => 1)))
