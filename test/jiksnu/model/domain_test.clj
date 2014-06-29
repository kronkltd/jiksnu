(ns jiksnu.model.domain-test
  (:require [clj-factory.core :refer [factory]]
            [clojure.tools.logging :as log]
            [jiksnu.actions.domain-actions :as actions.domain]
            [jiksnu.mock :as mock]
            [jiksnu.model :as model]
            [jiksnu.model.domain :refer [create drop! get-xrd-url]]
            [jiksnu.test-helper :refer [context test-environment-fixture]]
            [midje.sweet :refer [=> contains]])
  (:import jiksnu.model.Domain))

(test-environment-fixture

 (context #'create
   (let [params (actions.domain/prepare-create (factory :domain))]
     (create params) => (partial instance? Domain)))

 (context #'get-xrd-url
   (context "when the domain doesn't exist"
     (get-xrd-url nil "acct:foo@example.com") => nil?))

 )
