(ns jiksnu.actions.request-token-actions-test
  (:require [clj-factory.core :refer [factory fseq]]
            [jiksnu.actions.request-token-actions :as actions.request-token]
            [jiksnu.factory :as factory]
            [jiksnu.mock :as mock]
            [jiksnu.session :as session]
            [jiksnu.test-helper :refer [check context future-context test-environment-fixture]]
            [midje.sweet :refer [=>]])
  (:import jiksnu.model.RequestToken
           org.bson.types.ObjectId
           ))

(test-environment-fixture

 (context #'actions.request-token/create
   (let [client (mock/a-client-exists)
         params {:client (:_id client)
                 :callback (fseq :uri)}]
     (actions.request-token/create params) =>
     (check [token]
       token => (partial instance? RequestToken)
       (:_id token) => (partial instance? String))))

 ;; (context #'actions.request-token/get-request-token
 ;;   (let [params {}]
 ;;     (actions.request-token/get-request-token params) =>
 ;;     (check [token]
 ;;       token => (partial instance? RequestToken)
 ;;       )
 ;;     )
 ;;   )

 )