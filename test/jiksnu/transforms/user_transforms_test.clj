(ns jiksnu.transforms.user-transforms-test
  (:require [clj-factory.core :refer [factory fseq]]
            [clojure.tools.logging :as log]
            [jiksnu.mock :as mock]
            [jiksnu.model :as model]
            [jiksnu.ops :as ops]
            [jiksnu.test-helper :as th]
            [jiksnu.transforms.user-transforms :as transforms.user]
            [midje.sweet :refer :all])
  (:import jiksnu.model.Domain))

(namespace-state-changes
 [(before :contents (th/setup-testing))
  (after :contents (th/stop-testing))])

(fact "#'transforms.user/set-domain"
  (let [username (fseq :username)
        domain-name (fseq :domain)
        uri (format "acct:%s@%s" username domain-name)
        params {:_id uri}]
    (transforms.user/set-domain params) => map?))
