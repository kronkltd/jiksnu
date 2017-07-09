(ns jiksnu.modules.core.actions.request-token-actions-test
  (:require [clj-factory.core :refer [factory fseq]]
            [jiksnu.modules.core.actions.request-token-actions :as actions.request-token]
            [jiksnu.mock :as mock]
            [jiksnu.test-helper :as th]
            [midje.sweet :refer :all])
  (:import jiksnu.model.RequestToken))

(th/module-test ["jiksnu.modules.core"])

(facts "#'actions.request-token/create"
  (let [client (mock/a-client-exists)
        params {:client (:_id client)
                :callback (fseq :uri)}]
    (actions.request-token/create params) =>
    (every-checker
     (partial instance? RequestToken)
     (contains {:_id string?}))))

(facts "#'actions.request-token/get-request-token"
  (let [params {}]
    (actions.request-token/get-request-token params) =>
    (partial instance? RequestToken)))
