(ns jiksnu.model.like-test
  (:require [clj-factory.core :refer [factory fseq]]
            [clojure.tools.logging :as log]
            [jiksnu.actions.like-actions :as actions.like]
            [jiksnu.mock :as mock]
            [jiksnu.model.like :as model.like]
            [jiksnu.test-helper :as th]
            [midje.sweet :refer :all]))

(namespace-state-changes
 [(before :contents (th/setup-testing))
  (after :contents (th/stop-testing))])

(fact "#'model.like/delete"
  (let [like (mock/that-user-likes-this-activity)]
    (model.like/delete like)
    (model.like/fetch-by-id (:_id like)) => falsey))

(fact "#'model.like/fetch-all"
  (model.like/fetch-all) => seq?)


