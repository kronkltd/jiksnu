(ns jiksnu.actions.like-actions-test
  (:use [clj-factory.core :only [factory fseq]]
        [jiksnu.actions.like-actions :only [delete show]]
        [jiksnu.test-helper :only [check context future-context test-environment-fixture]]
        [midje.sweet :only [falsey => contains]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.mock :as mock]
            [jiksnu.model :as model]
            [jiksnu.model.like :as model.like]))

(test-environment-fixture

 (future-context #'show
   (let [tag-name (fseq :word)]
     (show tag-name) => seq?))

 (future-context #'delete
   (let [user (mock/a-user-exists)
         activity (mock/there-is-an-activity)
         like (model.like/create (factory :like
                                          {:user (:_id user)
                                           :activity (:_id activity)}))]
     (delete like)
     (model.like/fetch-by-id (:_id like)) => falsey))
 )
