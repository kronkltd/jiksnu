(ns jiksnu.model.conversation-test
  (:use [clj-factory.core :only [factory]]
        [jiksnu.test-helper :only [test-environment-fixture]]
        [jiksnu.session :only [with-user]]
        [jiksnu.model.conversation :only [count-records]]
        [midje.sweet :only [fact future-fact =>]]
        [validateur.validation :only [valid?]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.actions.conversation-actions :as actions.conversation]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.features-helper :as feature]
            [jiksnu.model :as model]))

(test-environment-fixture
 (fact "#'count-records"
   (fact "when there aren't any conversations"
     (count-records) => 0)
   (fact "when there are conversations"
     (let [n 15]
       (dotimes [i n]
         (actions.conversation/create (factory :conversation)))
       (count-records) => n))))
