(ns jiksnu.actions.feed-source-actions-test
  (:require [ciste.core :refer [with-context]]
            [ciste.model :as cm]
            [ciste.sections.default :refer [show-section]]
            [clj-factory.core :refer [factory fseq]]
            [clojure.tools.logging :as log]
            ;; [jiksnu.modules.atom.util :as abdera]
            [jiksnu.actions.domain-actions :as actions.domain]
            [jiksnu.actions.feed-source-actions :as actions.feed-source]
            [jiksnu.actions.resource-actions :as actions.resource]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.mock :as mock]
            [jiksnu.factory :as factory]
            [jiksnu.model :as model]
            [jiksnu.model.resource :as model.resource]
            [jiksnu.ops :as ops]
            [jiksnu.test-helper :refer [check context future-context test-environment-fixture]]
            [jiksnu.util :as util]
            [lamina.core :as l]
            [midje.sweet :refer [=> truthy anything]])
  (:import jiksnu.model.Activity
           jiksnu.model.FeedSource))

(test-environment-fixture

 (context #'actions.feed-source/add-watcher
   (let [domain (actions.domain/current-domain)
         user (mock/a-user-exists)
         source (mock/a-feed-source-exists {:domain domain})]
     (actions.feed-source/add-watcher source user) => truthy))

 (context #'actions.feed-source/create
   (let [domain (mock/a-remote-domain-exists)
         params (factory :feed-source {:topic (factory/make-uri (:_id domain))})]
     (actions.feed-source/create params) => (partial instance? FeedSource)
     (provided
       (actions.domain/get-discovered domain nil nil) => domain)))

 (future-context #'actions.feed-source/update
   (let [domain (mock/a-domain-exists)
         source (mock/a-feed-source-exists)]
     (actions.feed-source/update source) => (partial instance? FeedSource))
   (provided
     (actions.domain/get-discovered anything) => .domain.))

 (context #'actions.feed-source/discover-source
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

 )
