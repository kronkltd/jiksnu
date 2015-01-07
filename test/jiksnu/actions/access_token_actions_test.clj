(ns jiksnu.actions.access-token-actions-test
  (:require [clj-factory.core :refer [factory fseq]]
            [clojure.tools.logging :as log]
            [jiksnu.actions.access-token-actions :as actions.access-token]
            [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.actions.domain-actions :as actions.domain]
            [jiksnu.db :as db]
            [jiksnu.mock :as mock]
            [jiksnu.model :as model]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.session :as session]
            [jiksnu.test-helper :as th]
            [jiksnu.util :as util]
            [midje.sweet :refer :all])
  (:import jiksnu.model.AccessToken
           jiksnu.model.Activity))

(namespace-state-changes
 [(before :contents (th/setup-testing))
  (after :contents (th/stop-testing))])

(fact "#'actions.access-token/get-access-token"
  (let [client (mock/a-client-exists)
        token (mock/a-request-token-exists {:client client})]
    (let [params {"oauth_version" "1.0"
                  "oauth_consumer_key" (:_id client)
                  "oauth_token" (:_id token)
                  "oauth_signature_method" "HMAC-SHA1"}]
      (actions.access-token/get-access-token params) =>
      (partial instance? AccessToken))))


