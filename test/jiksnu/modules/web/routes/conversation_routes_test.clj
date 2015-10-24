(ns jiksnu.modules.web.routes.conversation-routes-test
  (:require [ciste.core :refer [with-context]]
            [ciste.sections.default :refer [full-uri]]
            [clj-factory.core :refer [fseq]]
            [clojure.data.json :as json]
            [clojure.tools.logging :as log]
            [clojurewerkz.support.http.statuses :as status]
            [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.actions.auth-actions :as actions.auth]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.mock :as mock]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.model.user :as model.user]
            jiksnu.modules.web.views.activity-views
            [jiksnu.routes-helper :refer [as-user response-for]]
            [jiksnu.test-helper :as th]
            [midje.sweet :refer :all]
            [ring.mock.request :as req])
  (:import jiksnu.model.User))

(namespace-state-changes
 [(before :contents (th/setup-testing))
  (after :contents (th/stop-testing))])

(facts "route: converation-api/activities-stream :get"
  (let [conversation (mock/a-conversation-exists)
        url (str "/model/conversations/" (:_id conversation) "/activities")
        request (req/request :get url)]
    (response-for request) =>
    (contains
     {:status 200})))
