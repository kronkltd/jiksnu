(ns jiksnu.actions.feed-source-actions-test
  (:use [clj-factory.core :only [factory fseq]]
        [jiksnu.actions.feed-source-actions :only [add-watcher create prepare-create]]
        [jiksnu.factory :only [make-uri]]
        [jiksnu.test-helper :only [test-environment-fixture]]
        [midje.sweet :only [=> contains every-checker fact future-fact truthy anything]])
  (:require [ciste.model :as cm]
            [clojure.tools.logging :as log]
            [jiksnu.actions.domain-actions :as actions.domain]
            [jiksnu.actions.feed-source-actions :as actions.feed-source]
            [jiksnu.actions.resource-actions :as actions.resource]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.existance-helpers :as existance]
            [jiksnu.factory :as factory]
            [jiksnu.features-helper :as feature]
            [jiksnu.model :as model]
            [jiksnu.model.feed-source :as model.feed-source]
            [jiksnu.model.user :as model.user])
  (:import jiksnu.model.FeedSource))

(test-environment-fixture

 (fact "#'add-watcher"
   (let [domain (actions.domain/current-domain)
         user (existance/a-user-exists)
         source (existance/a-feed-source-exists {:domain domain})]
     (add-watcher source user) => truthy))

 (fact "#'create"
   (let [domain (existance/a-remote-domain-exists)
         params (factory :feed-source {:topic (factory/make-uri (:_id domain))})]
     (create params) => (partial instance? FeedSource)
     (provided
       (actions.domain/get-discovered domain) => domain)))

 (future-fact "#'update"
   (let [domain (existance/a-domain-exists)
         source (existance/a-feed-source-exists)]
     (actions.feed-source/update source) => (partial instance? FeedSource))
   (provided
    (actions.domain/get-discovered anything) => .domain.))

 (fact "#'discover-source"
   (let [url (make-uri (:_id (actions.domain/current-domain)) (str "/" (fseq :word)))
         resource (existance/a-resource-exists {:url url})
         topic (str url ".atom")]
     (actions.feed-source/discover-source url) => (partial instance? FeedSource))
   (provided
    (actions.resource/update* resource) => .response.
    (actions.resource/response->tree .response.) => .tree.
    (actions.resource/get-links .tree.) => .links.
    (model/find-atom-link .links.) => topic))

 )
