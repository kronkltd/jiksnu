(ns jiksnu.actions.feed-source-actions-test
  (:use [ciste.core :only [with-context]]
        [ciste.sections.default :only [show-section]]
        [clj-factory.core :only [factory fseq]]
        [jiksnu.actions.feed-source-actions :only [add-watcher create discover-source prepare-create
                                                   process-entry process-feed unsubscribe update]]
        [jiksnu.factory :only [make-uri]]
        [jiksnu.test-helper :only [check context future-context test-environment-fixture]]
        [midje.sweet :only [=> truthy anything]])
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
            [jiksnu.ops :as ops]
            [jiksnu.util :as util]
            [lamina.core :as l])
  (:import jiksnu.model.Activity
           jiksnu.model.FeedSource))

(test-environment-fixture

 (context #'add-watcher
   (let [domain (actions.domain/current-domain)
         user (mock/a-user-exists)
         source (mock/a-feed-source-exists {:domain domain})]
     (add-watcher source user) => truthy))

 (context #'create
   (let [domain (mock/a-remote-domain-exists)
         params (factory :feed-source {:topic (factory/make-uri (:_id domain))})]
     (create params) => (partial instance? FeedSource)
     (provided
       (actions.domain/get-discovered domain nil nil) => domain)))

 (future-context #'update
   (let [domain (mock/a-domain-exists)
         source (mock/a-feed-source-exists)]
     (actions.feed-source/update source) => (partial instance? FeedSource))
   (provided
     (actions.domain/get-discovered anything) => .domain.))

 (context #'process-entry
   (with-context [:http :atom]
     (let [user (mock/a-user-exists)
           author (show-section user)
           entry (show-section (factory :activity {:id (fseq :uri)}))
           feed (abdera/make-feed*
                 {:title (fseq :title)
                  :entries [entry]
                  :author author})
           source (mock/a-feed-source-exists)]
       (process-entry [feed source entry]) => (partial instance? Activity))))

 (context #'process-feed
   (context "when the feed has no watchers"
     (let [domain (mock/a-domain-exists)
           source (mock/a-feed-source-exists {:domain domain})
           feed (abdera/make-feed*
                 {:title (fseq :title)
                  :entries []})]
       (process-feed source feed) => nil
       (provided
         (unsubscribe source) => nil))))

 (context #'discover-source
   (let [url (make-uri (:_id (actions.domain/current-domain)) (str "/" (fseq :word)))
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
