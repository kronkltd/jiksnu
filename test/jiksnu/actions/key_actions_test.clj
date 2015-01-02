(ns jiksnu.actions.key-actions-test
  (:require [ciste.model :as cm]
            [clj-factory.core :refer [factory fseq]]
            [clojure.tools.logging :as log]
            [jiksnu.actions.domain-actions :as actions.domain]
            [jiksnu.actions.key-actions :as actions.key]
            [jiksnu.mock :as mock]
            [jiksnu.factory :as factory]
            [jiksnu.test-helper :refer [check test-environment-fixture]]
            [midje.sweet :refer [=> fact]]))

(test-environment-fixture

 (fact #'actions.key/index
   (actions.key/index) => map?
   )

 )
