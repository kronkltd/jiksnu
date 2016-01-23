(ns jiksnu.model.stream-test
  (:require [clj-factory.core :refer [factory fseq]]
            [jiksnu.mock :as mock]
            [jiksnu.model.stream :as model.stream]
            [jiksnu.test-helper :as th]
            [midje.sweet :refer :all]
            [validateur.validation :refer [valid?]]))

(namespace-state-changes
 [(before :contents (th/setup-testing))
  (after :contents (th/stop-testing))])

(fact "#'model.stream/count-records"

  (fact "when there aren't any items"
    (model.stream/drop!)
    (model.stream/count-records) => 0)

  (fact "when there are items"
    (model.stream/drop!)
    (let [n 15]
      (dotimes [i n]
        (mock/a-stream-exists))
      (model.stream/count-records) => n))
  )
