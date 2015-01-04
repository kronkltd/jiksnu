(ns jiksnu.actions.like-actions-test
  (:require [clj-factory.core :refer [factory fseq]]
            [clojure.tools.logging :as log]
            [jiksnu.actions.like-actions :as actions.like]
            [jiksnu.mock :as mock]
            [jiksnu.model :as model]
            [jiksnu.model.like :as model.like]
            [jiksnu.test-helper :as th]
            [midje.sweet :refer [=> after before fact falsey future-fact
                                 namespace-state-changes]]))

(namespace-state-changes
 [(before :contents (th/setup-testing))
  (after :contents (th/stop-testing))])

(future-fact "#'actions.like/show"
  (let [tag-name (fseq :word)]
    (actions.like/show tag-name) => seq?))

(future-fact "#'actions.like/delete"
  (let [user (mock/a-user-exists)
        activity (mock/there-is-an-activity)
        like (model.like/create (factory :like
                                         {:user (:_id user)
                                          :activity (:_id activity)}))]
    (actions.like/delete like)
    (model.like/fetch-by-id (:_id like)) => falsey))

