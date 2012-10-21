(ns jiksnu.actions.feed-source-actions-test
  (:use [ciste.model :only [get-links]]
        [clj-factory.core :only [factory fseq]]
        [jiksnu.actions.feed-source-actions :only [add-watcher create set-domain]]
        [jiksnu.factory :only [make-uri]]
        [jiksnu.test-helper :only [test-environment-fixture]]
        [midje.sweet :only [=> contains every-checker fact future-fact truthy anything]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.actions.domain-actions :as actions.domain]
            [jiksnu.actions.feed-source-actions :as actions.feed-source]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.existance-helpers :as existance]
            [jiksnu.features-helper :as feature]
            [jiksnu.model :as model]
            [jiksnu.model.feed-source :as model.feed-source]
            [jiksnu.model.user :as model.user])
  (:import jiksnu.model.FeedSource))

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

 (fact "#'add-watcher"
   (let [domain (actions.domain/current-domain)
         user (existance/a-record-exists :user {:domain (:_id domain)})
         source (existance/a-record-exists :feed-source)]
     (add-watcher source user)) => truthy
     (provided
       (actions.domain/get-discovered anything) => .domain.))

 (fact "#'create"
   (let [params (factory :feed-source)]
     (create params) => (partial instance? FeedSource))
   (provided
     (actions.domain/get-discovered anything) => .domain.))

 (future-fact "#'update"
   (let [domain (existance/a-domain-exists)
         source (existance/a-record-exists :feed-source)]
     (actions.feed-source/update source) => (partial instance? FeedSource))
   (provided
     (actions.domain/get-discovered anything) => .domain.))

 (fact "#'discover-source"
   (let [url (make-uri (:_id (actions.domain/current-domain)) (str "/" (fseq :word)))
         topic (str url ".atom")]
     (actions.feed-source/discover-source url) => (partial instance? FeedSource))
   (provided
     (model/extract-atom-link url) => {:href topic}))

 )
