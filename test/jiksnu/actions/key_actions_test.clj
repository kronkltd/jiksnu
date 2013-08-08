(ns jiksnu.actions.key-actions-test
  (:use [clj-factory.core :only [factory fseq]]
        [clj-tigase.core :only [deliver-packet!]]
        [jiksnu.test-helper :only [check context future-context test-environment-fixture]]
        jiksnu.actions.key-actions
        midje.sweet)
  (:require [ciste.model :as cm]
            [clj-tigase.packet :as packet]
            [clojure.tools.logging :as log]
            [jiksnu.actions.domain-actions :as actions.domain]
            [jiksnu.actions.resource-actions :as actions.resource]
            [jiksnu.mock :as mock]
            [jiksnu.factory :as factory]))

(test-environment-fixture

 (context #'index
   (index) => map?
   )

 )
