(ns jiksnu.transforms.user-transforms-test
  (:require [clj-factory.core :refer [factory fseq]]
            [jiksnu.test-helper :as th]
            [jiksnu.transforms.user-transforms :as transforms.user]
            [midje.sweet :refer :all]))

(th/module-test ["jiksnu.modules.core"])

(fact "#'transforms.user/set-domain"
  (let [username (fseq :username)
        domain-name (fseq :domain)
        uri (format "acct:%s@%s" username domain-name)
        params {:_id uri}]
    (transforms.user/set-domain params) => map?))
