(ns jiksnu.modules.admin.actions.like-actions-test
  (:require [clj-factory.core :refer [factory]]
            [jiksnu.model.like :as model.like]
            [jiksnu.modules.admin.actions.like-actions :as actions.like]
            [jiksnu.test-helper :as th]
            [midje.sweet :refer :all]))

(namespace-state-changes
 [(before :contents (th/setup-testing))
  (after :contents (th/stop-testing))])

(fact "#'actions.like/index"
  (actions.like/index) => (contains {:page 1 :totalItems 0})

  (let [response (actions.like/index {} {:page 2})]
    (:page response) => 2
    (:totalItems response) => 0))

(future-fact "#'actions.like/delete"
  (let [like (model.like/create (factory :like))]
    (actions.like/delete like)
    (model.like/fetch-by-id (:_id like)) => falsey))

