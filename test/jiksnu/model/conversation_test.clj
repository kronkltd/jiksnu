(ns jiksnu.model.conversation-test
  (:use [clj-factory.core :only [factory]]
        [jiksnu.test-helper :only [test-environment-fixture]]
        [jiksnu.session :only [with-user]]
        [jiksnu.model.conversation :only [count-records]]
        [midje.sweet :only [fact future-fact =>]]
        [validateur.validation :only [valid?]])
  (:require [clojure.tools.logging :as log]
            [jiksnu.actions.user-actions :as actions.user]
            [jiksnu.features-helper :as feature]
            [jiksnu.model :as model]
            )
  )

(test-environment-fixture
 (fact "#'count-records"
   (count-records) => 0
   )
 )
