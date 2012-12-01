(ns jiksnu.actions.feed-subscription-actions-test
  (:use [clj-factory.core :only [factory fseq]]
        [jiksnu.actions.feed-subscription-actions :only [create delete exists? index
                                                         prepare-create]]
        [jiksnu.factory :only [make-uri]]
        [jiksnu.test-helper :only [test-environment-fixture]]
        [midje.sweet :only [=> contains every-checker fact falsey future-fact truthy anything]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.actions.domain-actions :as actions.domain]
            [jiksnu.actions.feed-source-actions :as actions.feed-source]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.existance-helpers :as existance]
            [jiksnu.features-helper :as feature]
            [jiksnu.model :as model]
            [jiksnu.model.feed-source :as model.feed-source]
            [jiksnu.model.feed-subscription :as model.feed-subscription]
            [jiksnu.model.user :as model.user])
  (:import jiksnu.model.FeedSubscription))

(test-environment-fixture

 (fact "#'delete"
   (let [item (existance/a-feed-subscription-exists)]
     (delete item)

     (exists? item) => falsey
     )
   )

 (fact "#'create"
   (let [params (prepare-create (factory :feed-subscription))]
     (create params) => (partial instance? FeedSubscription)
     )
   )

 (fact "#'index"
   (model.feed-subscription/drop!)
   (:items (index)) => []
   )

 )
