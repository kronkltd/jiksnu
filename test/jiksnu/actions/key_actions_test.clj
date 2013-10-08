(ns jiksnu.actions.key-actions-test
  (:require [ciste.model :as cm]
            [clj-factory.core :refer [factory fseq]]
            [clj-tigase.core :refer [deliver-packet!]]
            [clj-tigase.packet :as packet]
            [clojure.tools.logging :as log]
            [jiksnu.actions.domain-actions :as actions.domain]
            [jiksnu.actions.resource-actions :as actions.resource]
            [jiksnu.mock :as mock]
            [jiksnu.factory :as factory]
            [jiksnu.test-helper :refer [check context future-context test-environment-fixture]]
            [midje.sweet :refer [=>]]))

(test-environment-fixture

 (context #'index
   (index) => map?
   )

 )
