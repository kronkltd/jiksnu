(ns jiksnu.actions.feed-source-actions-test
  (:use [clj-factory.core :only [factory]]
        [jiksnu.actions.feed-source-actions :only [add-watcher create]]
        [jiksnu.test-helper :only [test-environment-fixture]]
        [midje.sweet :only [fact future-fact => every-checker truthy]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.actions.domain-actions :as actions.domain]
            [jiksnu.actions.feed-source-actions :as actions.feed-source]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.existance-helpers :as existance]
            [jiksnu.features-helper :as feature]
            [jiksnu.model.feed-source :as model.feed-source]
            [jiksnu.model.user :as model.user])
  (:import jiksnu.model.FeedSource))

(test-environment-fixture

 (fact "#'add-watcher"
   (let [domain (actions.domain/current-domain)
         user (existance/a-record-exists :user {:domain (:_id domain)})
         source (existance/a-record-exists :feed-source)]
     (add-watcher source user)) => truthy)

 (fact "#'create"
   (let [params (factory :feed-source)]
     (create params) => (partial instance? FeedSource)))

 (fact "#'update"
   (let [source (existance/a-record-exists :feed-source)]
     (actions.feed-source/update source) => (partial instance? FeedSource)))

 )
