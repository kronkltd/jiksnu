(ns jiksnu.transforms.user-transforms-test
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
            [lamina.core :as l]
            [midje.sweet :refer [=> anything]])
  (:import jiksnu.model.Domain))

(test-environment-fixture

 (context #'transforms.user/set-domain
   (let [username (fseq :username)
         domain-name (fseq :domain)
         uri (format "acct:%s@%s" username domain-name)
         params {:_id uri}]
     (transforms.user/set-domain params) =>
     (check [response]
       response => map?
       )
     (provided
       (ops/get-discovered anything) => (l/success-result (model/map->Domain {:_id domain-name})
                                              )
       )
     )
   )

 )