(ns jiksnu.actions.request-token-actions-test
  (:require [clj-factory.core :refer [factory fseq]]
            [jiksnu.actions.request-token-actions :as actions.request-token]
            [jiksnu.factory :as factory]
            [jiksnu.mock :as mock]
            [jiksnu.test-helper :as th]
            [midje.sweet :refer :all])
  (:import jiksnu.model.RequestToken
           org.bson.types.ObjectId
           ))

(namespace-state-changes
 [(before :contents (th/setup-testing))
  (after :contents (th/stop-testing))])

(fact #'actions.request-token/create
  (let [client (mock/a-client-exists)
        params {:client (:_id client)
                :callback (fseq :uri)}]
    (actions.request-token/create params) =>
    (th/check [token]
           token => (partial instance? RequestToken)
           (:_id token) => (partial instance? String))))

;; (fact #'actions.request-token/get-request-token
;;   (let [params {}]
;;     (actions.request-token/get-request-token params) =>
;;     (th/check [token]
;;       token => (partial instance? RequestToken)
;;       )
;;     )
;;   )


