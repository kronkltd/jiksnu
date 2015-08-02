(ns jiksnu.actions.feed-source-actions-test
  (:require [ciste.model :as cm]
            [ciste.sections.default :refer [show-section]]
            [clj-factory.core :refer [factory fseq]]
            [clojure.tools.logging :as log]
            [jiksnu.actions.domain-actions :as actions.domain]
            [jiksnu.actions.feed-source-actions :as actions.feed-source]
            [jiksnu.actions.resource-actions :as actions.resource]
            [jiksnu.actions.service-actions :as actions.service]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.mock :as mock]
            [jiksnu.factory :as factory]
            [jiksnu.model :as model]
            [jiksnu.model.resource :as model.resource]
            [jiksnu.ops :as ops]
            [jiksnu.test-helper :as th]
            [jiksnu.util :as util]
            [lamina.core :as l]
            [midje.sweet :refer :all])
  (:import jiksnu.model.FeedSource))

(namespace-state-changes
 [(before :contents (th/setup-testing))
  (after :contents (th/stop-testing))])

(fact "#'actions.feed-source/add-watcher"
  (let [domain (actions.domain/current-domain)
        user (mock/a-user-exists)
        source (mock/a-feed-source-exists {:domain domain})]
    (actions.feed-source/add-watcher source user) => truthy))

(fact "#'actions.feed-source/create"
  (let [domain (mock/a-remote-domain-exists)
        params (factory :feed-source {:topic (factory/make-uri (:_id domain))})]
    (actions.feed-source/create params) => (partial instance? FeedSource)
    (provided
      (actions.service/get-discovered domain nil nil) => domain)))

(future-fact "#'actions.feed-source/update-record"
  (let [domain (mock/a-domain-exists)
        source (mock/a-feed-source-exists)]
    (actions.feed-source/update-record source) => (partial instance? FeedSource))
  (provided
    (actions.service/get-discovered anything) => .domain.))

(fact "#'actions.feed-source/discover-source"
  (let [url (factory/make-uri (:_id (actions.domain/current-domain)) (str "/" (fseq :word)))
        resource (mock/a-resource-exists {:url url})
        topic (str url ".atom")
        response ""
        result (l/result-channel)]
    (l/enqueue result response)
    (actions.feed-source/discover-source url) => (partial instance? FeedSource)
    (provided
      (ops/update-resource url) => result
      (model.resource/response->tree response) => .tree.
      (model.resource/get-links .tree.) => .links.
      (util/find-atom-link .links.) => topic)))
