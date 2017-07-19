(ns jiksnu.modules.core.actions.like-actions-test
  (:require [clj-factory.core :refer [factory fseq]]
            [jiksnu.modules.core.actions.like-actions :as actions.like]
            [jiksnu.mock :as mock]
            [jiksnu.modules.core.model.like :as model.like]
            [jiksnu.test-helper :as th]
            [midje.sweet :refer :all]))

(th/module-test ["jiksnu.modules.core"])

(fact "#'jiksnu.modules.core.actions.actions.like-actions/show"
  (let [tag-name (fseq :word)]
    (actions.like/show tag-name) => tag-name))

(fact "#'jiksnu.modules.core.actions.like-actions/delete"
  (let [user (mock/a-user-exists)
        activity (mock/an-activity-exists)
        params {:user (:_id user) :activity (:_id activity)}
        like (actions.like/create (factory :like params))]
    (actions.like/delete like)
    (model.like/fetch-by-id (:_id like)) => falsey))
