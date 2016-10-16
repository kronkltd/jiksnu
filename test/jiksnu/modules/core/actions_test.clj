(ns jiksnu.modules.core.actions-test
  (:require [jiksnu.mock :as mock]
            [jiksnu.modules.core.actions :as actions]
            [jiksnu.test-helper :as th]
            [jiksnu.util :as util]
            [midje.sweet :refer :all]))

(th/module-test ["jiksnu.modules.core"
                 "jiksnu.modules.web"])

(facts "#'actions/get-page"
  (facts "users"
    (actions/get-page "users") => (contains {:totalItems integer?}))
  (facts "pictures"
    (actions/get-page "pictures") => (contains {:totalItems integer?})))
