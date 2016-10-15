(ns jiksnu.actions.group-membership-actions-test
  (:require [ciste.config :refer [config]]
            [clj-factory.core :refer [factory fseq]]
            [jiksnu.actions.group-membership-actions :as actions.group-membership]
            [jiksnu.mock :as mock]
            [jiksnu.test-helper :as th]
            [midje.sweet :refer :all]))

(th/module-test ["jiksnu.modules.core"])

(fact "fetch-by-group"
  (let [user (mock/a-user-exists)
        group (mock/a-group-exists)]

    (actions.group-membership/create
     {:user (:_id user)
      :group (:_id group)})

    (actions.group-membership/fetch-by-group group) =>
    (contains {:totalItems 1})))
