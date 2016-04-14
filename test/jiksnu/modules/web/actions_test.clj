(ns jiksnu.modules.web.actions-test
  (:require [jiksnu.modules.core.actions :refer [get-sub-page]]
            [jiksnu.mock :as mock]
            [midje.sweet :refer :all]
            [jiksnu.test-helper :as th]))

(th/module-test ["jiksnu.modules.core"
                 "jiksnu.modules.web"])

(fact "get-sub-page"
  (fact "User activities"
    (let [user (mock/a-user-exists)
          page-name "activities"
          m 1]
      (dotimes [n m] (mock/there-is-an-activity :user user))

      (get-sub-page user page-name) =>
      (contains {:totalItems m}))))
