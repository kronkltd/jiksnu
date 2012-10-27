(ns jiksnu.transforms.feed-source-transforms-test
  (:use [clj-factory.core :only [factory fseq]]
        [jiksnu.transforms.feed-source-transforms :only [set-domain]]
        [jiksnu.factory :only [make-uri]]
        [jiksnu.test-helper :only [test-environment-fixture]]
        [midje.sweet :only [=> contains every-checker fact future-fact truthy anything]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.actions.feed-source-actions :as actions.feed-source]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.existance-helpers :as existance]
            [jiksnu.features-helper :as feature]
            [jiksnu.model :as model]
            [jiksnu.model.feed-source :as model.feed-source]
            [jiksnu.model.user :as model.user]))

(test-environment-fixture

 (fact "#'set-domain"
   (fact "when the source has a domain"
     (let [domain (existance/a-record-exists :domain)
           source (factory :feed-source {:domain (:_id domain)})]
       (set-domain source) => source))
   (fact "when the source does not have a domain"
     (let [domain (existance/a-record-exists :domain)
           url (make-uri (:_id domain))
           source (dissoc (factory :feed-source {:topic url}) :domain)]
       (set-domain source) => (contains {:domain (:_id domain)})))

   (provided
     (actions.domain/get-discovered anything) => {:_id (:_id domain)}))

)
