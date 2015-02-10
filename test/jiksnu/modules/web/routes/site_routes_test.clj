(ns jiksnu.modules.web.routes.site-routes-test
  (:require [ciste.formats :refer [format-as]]
            [ciste.model :as cm]
            [clj-factory.core :refer [factory fseq]]
            [clojure.tools.logging :as log]
            [clojurewerkz.support.http.statuses :as status]
            [jiksnu.actions.auth-actions :as actions.auth]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.db :as db]
            [jiksnu.mock :as mock]
            [jiksnu.model :as model]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.subscription :as model.subscription]
            [jiksnu.model.user :as model.user]
            [jiksnu.test-helper :refer [test-environment-fixture]]
            [jiksnu.routes-helper :refer [as-user response-for]]
            [midje.sweet :refer :all]
            [ring.mock.request :as req]))

(test-environment-fixture
 (future-fact "rsd document"
   (let [response (-> (req/request :get "/rsd.xml") response-for)]
     response => map?
     (:status response) => status/success?
     (let [body (cm/string->document (:body response))
           root (.getRootElement body)
           attr {"rsd" "http://archipelago.phrasewise.com/rsd"}
           nodes (cm/query root "//rsd:rsd" attr)]
       (count nodes) => 1)))
 )
