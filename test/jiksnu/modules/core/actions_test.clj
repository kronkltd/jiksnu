(ns jiksnu.modules.core.actions-test
  (:require [jiksnu.mock :as mock]
            [jiksnu.modules.core.actions :as actions]
            [jiksnu.test-helper :as th]
            [midje.sweet :refer :all]))

(th/module-test ["jiksnu.modules.core"
                 "jiksnu.modules.web"])

(facts "#'jiksnu.modules.core.actions/get-model"
  (facts "activity"
    (fact "when the item exists"
      (let [activity (mock/an-activity-exists)
            id (:_id activity)]
        (actions/get-model "activity" id) =>
        (contains {:_id id})))
    (fact "when the item exists"
      (let [activity (mock/an-activity-exists)
            id (:_id activity)]
        (actions/get-model "activity" (str id)) =>
        (contains {:_id id})))))

(facts "#'jiksnu.modules.core.actions/get-page"
  (facts "users"
    (actions/get-page "users") => (contains {:totalItems integer?}))
  (facts "notifications"
    (actions/get-page "notifications") => (contains {:totalItems integer?}))
  (facts "pictures"
    (actions/get-page "pictures") => (contains {:totalItems integer?})))

(facts "#'jiksnu.modules.core.actions/get-sub-page"
  (fact "Activity likes"
    (let [user (mock/a-user-exists)
          activity (mock/an-activity-exists :user user)
          page-name "likes"]
      (actions/get-sub-page activity page-name) =>
      (contains {:totalItems 0
                 :items []})))
  (fact "User activities"
    (let [user (mock/a-user-exists)
          page-name "activities"
          m 1]
      (dotimes [_ m] (mock/an-activity-exists :user user))

      (actions/get-sub-page user page-name) =>
      (contains {:totalItems m}))))
