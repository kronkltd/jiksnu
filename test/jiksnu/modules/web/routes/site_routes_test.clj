(ns jiksnu.modules.web.routes.site-routes-test
  (:use [ciste.formats :only [format-as]]
        [clj-factory.core :only [factory fseq]]
        [jiksnu.test-helper :only [check context future-context test-environment-fixture]]
        [jiksnu.routes-helper :only [as-user response-for]]
        [midje.sweet :only [=>]])
  (:require [ciste.model :as cm]
            [clojure.tools.logging :as log]
            [clojurewerkz.support.http.statuses :as status]
            [hiccup.core :as h]
            [jiksnu.actions.auth-actions :as actions.auth]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.db :as db]
            [jiksnu.mock :as mock]
            [jiksnu.features-helper :as feature]
            [jiksnu.model :as model]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.subscription :as model.subscription]
            [jiksnu.model.user :as model.user]
            [ring.mock.request :as req]))

(test-environment-fixture
 (context "rsd document"
   (-> (req/request :get "/rsd.xml") response-for) =>
   (check [response]
     response => map?
     (:status response) => status/success?
     (let [body (cm/string->document (:body response))
           root (.getRootElement body)
           context {"rsd" "http://archipelago.phrasewise.com/rsd"}
           nodes (cm/query root "//rsd:rsd" context)]
       (count nodes) => 1)))
 )
