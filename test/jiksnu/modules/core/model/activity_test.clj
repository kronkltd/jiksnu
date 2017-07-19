(ns jiksnu.modules.core.model.activity-test
  (:require [clj-factory.core :refer [factory fseq]]
            [jiksnu.modules.core.actions.activity-actions :as actions.activity]
            [jiksnu.mock :as mock]
            [jiksnu.modules.core.model.activity :as model.activity]
            [jiksnu.modules.core.factory :as f]
            [jiksnu.test-helper :as th]
            [midje.sweet :refer :all]
            [validateur.validation :refer [valid?]])
  (:import jiksnu.modules.core.model.Activity))

(th/module-test ["jiksnu.modules.core"])

(fact "#'model.activity/count-records"

  (fact "when there aren't any items"
    (model.activity/drop!)
    (model.activity/count-records) => 0)

  (fact "when there are conversations"
    (model.activity/drop!)
    (let [n 15]
      (dotimes [i n]
        (mock/an-activity-exists))
      (model.activity/count-records) => n)))

(fact "#'model.activity/create"
  (let [domain (mock/a-domain-exists)
        feed-source (mock/a-feed-source-exists {:domain domain})
        conversation (mock/a-conversation-exists {:feed-source feed-source})
        id (f/make-uri (:_id domain) (fseq :path))
        activity (actions.activity/prepare-create
                  (factory :full-activity {:conversation (:_id conversation)
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
          activity (mock/an-activity-exists {:user user})]
      (model.activity/get-author activity) => user)))
