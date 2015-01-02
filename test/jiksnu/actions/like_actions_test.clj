(ns jiksnu.actions.like-actions-test
  (:require [clj-factory.core :refer [factory fseq]]
            [clojure.tools.logging :as log]
            [jiksnu.actions.like-actions :refer [delete show]]
            [jiksnu.mock :as mock]
            [jiksnu.model :as model]
            [jiksnu.model.like :as model.like]
            [jiksnu.test-helper :refer [check test-environment-fixture]]
            [midje.sweet :refer [falsey => fact future-fact]]))

(test-environment-fixture

 (future-fact #'show
   (let [tag-name (fseq :word)]
     (show tag-name) => seq?))

 (future-fact #'delete
   (let [user (mock/a-user-exists)
         activity (mock/there-is-an-activity)
         like (model.like/create (factory :like
                                          {:user (:_id user)
                                           :activity (:_id activity)}))]
     (delete like)
     (model.like/fetch-by-id (:_id like)) => falsey))
 )
