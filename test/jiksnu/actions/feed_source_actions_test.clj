(ns jiksnu.actions.feed-source-actions-test
  (:use [clj-factory.core :only [factory fseq]]
        [jiksnu.actions.feed-source-actions :only [add-watcher create prepare-create process-feed]]
        [jiksnu.factory :only [make-uri]]
        [jiksnu.test-helper :only [test-environment-fixture]]
        [midje.sweet :only [=> contains every-checker fact future-fact truthy anything]])
  (:require [ciste.model :as cm]
            [clojure.tools.logging :as log]
            [jiksnu.abdera :as abdera]
            [jiksnu.actions.domain-actions :as actions.domain]
            [jiksnu.actions.feed-source-actions :as actions.feed-source]
            [jiksnu.actions.resource-actions :as actions.resource]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.mock :as mock]
            [jiksnu.factory :as factory]
            [jiksnu.features-helper :as feature]
            [jiksnu.model :as model]
            [jiksnu.model.feed-source :as model.feed-source]
            [jiksnu.model.resource :as model.resource]
            [jiksnu.model.user :as model.user]
            [jiksnu.util :as util])
  (:import jiksnu.model.FeedSource))

(test-environment-fixture

 (fact "#'add-watcher"
   (let [domain (actions.domain/current-domain)
         user (mock/a-user-exists)
         source (mock/a-feed-source-exists {:domain domain})]
     (add-watcher source user) => truthy))

 (fact "#'create"
   (let [domain (mock/a-remote-domain-exists)
         params (factory :feed-source {:topic (factory/make-uri (:_id domain))})]
     (create params) => (partial instance? FeedSource)
     (provided
       (actions.domain/get-discovered domain) => domain)))

 (future-fact "#'update"
   (let [domain (mock/a-domain-exists)
         source (mock/a-feed-source-exists)]
     (actions.feed-source/update source) => (partial instance? FeedSource))
   (provided
     (actions.domain/get-discovered anything) => .domain.))

 (fact "#'process-feed"
   (let [source (mock/a-feed-source-exists)
         feed (abdera/make-feed*
               {:title (fseq :title)
                :entries []})]
     (process-feed source feed) => nil))

 (fact "#'discover-source"
   (let [url (make-uri (:_id (actions.domain/current-domain)) (str "/" (fseq :word)))
         resource (mock/a-resource-exists {:url url})
         topic (str url ".atom")]
     (actions.feed-source/discover-source url) => (partial instance? FeedSource)
     (provided
       (actions.resource/update* resource) => .response.
       (model.resource/response->tree .response.) => .tree.
       (model.resource/get-links .tree.) => .links.
       (util/find-atom-link .links.) => topic)))

 )
