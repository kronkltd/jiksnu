(ns jiksnu.actions.client-actions-test
  (:require [clj-factory.core :refer [factory fseq]]
            [clojure.tools.logging :as log]
            [jiksnu.actions.client-actions :as actions.client]
            [jiksnu.mock :as mock]
            [jiksnu.test-helper :refer [test-environment-fixture]]
            [midje.sweet :refer [=> fact]])
  (:import jiksnu.model.Client))

(test-environment-fixture

 (fact #'actions.client/create
   (let [params (factory :client)
         response (actions.client/create params)]
     (fact "should return a client"
       response => (partial instance? Client))))

 )
