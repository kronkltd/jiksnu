(ns jiksnu.actions.request-token-actions-test
  (:require [clj-factory.core :refer [factory]]
            [jiksnu.actions.request-token-actions :as actions.request-token]
            [jiksnu.mock :as mock]
            [jiksnu.session :as session]
            [jiksnu.test-helper :refer [check context future-context test-environment-fixture]]
            [midje.sweet :refer [=>]])
  (:import jiksnu.model.RequestToken
           org.bson.types.ObjectId
           ))

(test-environment-fixture

 (context #'actions.request-token/create
   (let [params {}]
     (actions.request-token/create params) =>
     (check [token]
       token => (partial instance? RequestToken)
       (:_id token) => (partial instance? ObjectId)
       )
     )
   )

 (context #'actions.request-token/get-request-token
   (let [params {}]
     (actions.request-token/get-request-token params) =>
     (check [token]
       token => (partial instance? RequestToken)
       )
     )
   )

 )



