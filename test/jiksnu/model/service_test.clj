(ns jiksnu.model.service-test
  (:require [clj-factory.core :refer [factory fseq]]
            [jiksnu.mock :as mock]
            [jiksnu.model.service :as model.service]
            [jiksnu.test-helper :as th]
            [jiksnu.util :as util]
            [midje.sweet :refer :all]
            [validateur.validation :refer [valid?]])
  (:import jiksnu.model.Service))

(th/module-test ["jiksnu.modules.core"])

(facts "#'jiksnu.model.service/fetch-by-id"
  (fact "when the activity exists"
    (let [item (mock/a-service-exists)
          id (:_id item)]
      (let [response (model.service/fetch-by-id id)]
        response => (partial instance? Service)
        response => (contains {:_id id}))))
  (fact "when the activity does not exist"
    (let [id (util/make-id)
          response (model.service/fetch-by-id id)]
      response => nil)))
