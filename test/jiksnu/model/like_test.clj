(ns jiksnu.model.like-test
  (:require [clj-factory.core :refer [factory]]
            [jiksnu.mock :as mock]
            [jiksnu.model.like :as model.like]
            [jiksnu.test-helper :as th]
            [midje.sweet :refer :all]))

(th/module-test ["jiksnu.modules.core"])

(fact "#'model.like/delete"
  (let [like (mock/that-user-likes-this-activity)]
    (model.like/delete like)
    (model.like/fetch-by-id (:_id like)) => falsey))

(fact "#'model.like/fetch-all"
  (model.like/fetch-all) => seq?)
