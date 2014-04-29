(ns jiksnu.actions.client-actions-test
  (:require [ciste.model :as cm]
            [clj-factory.core :refer [factory fseq]]
            [clojure.tools.logging :as log]
            [jiksnu.actions.client-actions :as actions.client]
            [jiksnu.actions.resource-actions :as actions.resource]
            [jiksnu.factory :as factory]
            [jiksnu.mock :as mock]
            [jiksnu.model :as model]
            [jiksnu.model.domain :as model.domain]
            [jiksnu.ops :as ops]
            [jiksnu.test-helper :refer [check context future-context test-environment-fixture]]
            [lamina.core :as l]
            [lamina.trace :as trace]
            [midje.sweet :refer [=> anything contains truthy]])
  (:import jiksnu.model.Client))

(test-environment-fixture

 (context #'actions.client/create

   (let [params (factory :client)]

     (actions.client/create params) =>
     (check [response]
       response => (partial instance? Client)
       )
     )

   )

 )
