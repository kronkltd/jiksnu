(ns jiksnu.modules.core.actions.feed-source-actions-test
  (:require [clj-factory.core :refer [factory fseq]]
            [jiksnu.modules.core.actions.domain-actions :as actions.domain]
            [jiksnu.modules.core.actions.feed-source-actions :as actions.feed-source]
            [jiksnu.modules.core.actions.service-actions :as actions.service]
            [jiksnu.mock :as mock]
            [jiksnu.model.resource :as model.resource]
            [jiksnu.modules.core.factory :as f]
            [jiksnu.ops :as ops]
            [jiksnu.test-helper :as th]
            [jiksnu.util :as util]
            [manifold.deferred :as d]
            [midje.sweet :refer :all])
  (:import jiksnu.model.FeedSource))

(th/module-test ["jiksnu.modules.core"])

(fact "#'actions.feed-source/add-watcher"
  (let [domain (actions.domain/current-domain)
        user (mock/a-user-exists)
        source (mock/a-feed-source-exists {:domain domain})]
    (actions.feed-source/add-watcher source user) => truthy))

(fact "#'actions.feed-source/create"
  (let [domain (mock/a-remote-domain-exists)
        params (factory :feed-source {:topic (f/make-uri (:_id domain))})]
    (actions.feed-source/create params) => (partial instance? FeedSource)
    (provided
     (actions.service/get-discovered domain nil nil) => domain)))

(fact "#'actions.feed-source/update-record"
  (let [domain (mock/a-domain-exists)
        source (mock/a-feed-source-exists)]
    (actions.feed-source/update-record source) => (partial instance? FeedSource)))

(fact "#'actions.feed-source/discover-source"
  (let [url (f/make-uri (:_id (actions.domain/current-domain)) (str "/" (fseq :word)))
        resource (mock/a-resource-exists {:url url})
        topic (str url ".atom")
        response ""
        result (d/success-deferred response)]
    (actions.feed-source/discover-source url) => (partial instance? FeedSource)
    (provided
     (ops/update-resource url) => result
     (model.resource/response->tree response) => .tree.
     (model.resource/get-links .tree.) => .links.
     (util/find-atom-link .links.) => topic)))
