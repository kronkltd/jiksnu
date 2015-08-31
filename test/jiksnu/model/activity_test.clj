(ns jiksnu.model.activity-test
  (:require [clj-factory.core :refer [factory fseq]]
            [clojure.tools.logging :as log]
            [jiksnu.actions.activity-actions :as actions.activity]
            [jiksnu.mock :as mock]
            [jiksnu.factory :as factory]
            [jiksnu.model :as model]
            [jiksnu.model.activity :as model.activity]
            [jiksnu.test-helper :as th]
            [midje.sweet :refer :all]
            [validateur.validation :refer [valid?]])
  (:import jiksnu.model.Activity
           jiksnu.model.User))

(namespace-state-changes
 [(before :contents (th/setup-testing))
  (after :contents (th/stop-testing))])

(fact "#'model.activity/count-records"

  (fact "when there aren't any items"
    (model.activity/drop!)
    (model.activity/count-records) => 0)

  (fact "when there are conversations"
    (model.activity/drop!)
    (let [n 15]
      (dotimes [i n]
        (mock/there-is-an-activity))
      (model.activity/count-records) => n))
  )

(fact "#'model.activity/create"
  (let [domain (mock/a-domain-exists)
        feed-source (mock/a-feed-source-exists {:domain domain})
        conversation (mock/a-conversation-exists {:feed-source feed-source})
        id (factory/make-uri (:_id domain) (fseq :path))
        activity (actions.activity/prepare-create
                  (factory :activity {:conversation (:_id conversation)
                                      :id id
                                      :local false
                                      :update-source (:_id feed-source)}))]
    (model.activity/create activity) => (partial instance? Activity)))

(fact "#'model.activity/get-author"

  (fact "when given an empty activity"
    (let [item (Activity.)]
      (model.activity/get-author item) => nil))

  (fact "when given a real activity"
    (let [user (mock/a-user-exists)
          activity (mock/there-is-an-activity {:user user})]
      (model.activity/get-author activity) => user))
  )


