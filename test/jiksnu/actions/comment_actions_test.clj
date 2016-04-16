(ns jiksnu.actions.comment-actions-test
  (:require [jiksnu.actions.comment-actions :refer :all]
            [jiksnu.mock :as mock]
            [jiksnu.test-helper :as th]
            [midje.sweet :refer :all]))

(th/module-test ["jiksnu.modules.core"])

(fact #'fetch-comments
  (fact "when the activity exists"
    (fact "and there are no comments"
      (let [actor (mock/a-user-exists)
            activity (mock/there-is-an-activity)
            [_ comments] (fetch-comments activity)]
        comments => empty?))))
