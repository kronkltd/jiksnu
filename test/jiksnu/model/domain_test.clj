(ns jiksnu.model.domain-test
  (:use [clj-factory.core :only [factory]]
        [midje.sweet :only [=> contains]]
        [jiksnu.test-helper :only [context test-environment-fixture]]
        [jiksnu.model.domain :only [create drop! get-xrd-url ping-request]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.actions.domain-actions :as actions.domain]
            [jiksnu.mock :as mock]
            [jiksnu.model :as model])
  (:import jiksnu.model.Domain))

(test-environment-fixture

 (context #'create
   (let [params (actions.domain/prepare-create (factory :domain))]
     (create params) => (partial instance? Domain)))

 ;; (context #'ping-request
 ;;   (drop!)
 ;;   (let [domain (mock/a-domain-exists)]
 ;;     (ping-request domain) => (contains {:body e/element?})))

 (context #'get-xrd-url
   (context "when the domain doesn't exist"
     (get-xrd-url nil "acct:foo@example.com") => nil?))

 )
