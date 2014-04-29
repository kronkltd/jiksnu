(ns jiksnu.ops-test
  (:require [clj-factory.core :refer [factory fseq]]
            [clojure.tools.logging :as log]
            [jiksnu.actions.domain-actions :as actions.domain]
            [jiksnu.mock :as mock]
            [jiksnu.model :as model]
            [jiksnu.ops :as ops]
            [jiksnu.session :as session]
            [jiksnu.test-helper :refer [check context future-context
                                        test-environment-fixture]]
            [jiksnu.transforms.user-transforms :as transforms.user]
            [midje.sweet :refer [=>]])
  (:import jiksnu.model.Domain))


(test-environment-fixture

 ;; (context #'ops/get-discovered
 ;;   (let [domain (mock/a-domain-exists)]
 ;;     (ops/get-discovered domain) =>
 ;;     (check [response]
 ;;       @response => (partial instance? Domain)
 ;;       )
 ;;     )
 ;;   )

 )
