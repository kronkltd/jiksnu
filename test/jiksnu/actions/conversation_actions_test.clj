(ns jiksnu.actions.conversation-actions-test
  (:require [clj-factory.core :refer [factory]]
            [jiksnu.actions.conversation-actions :refer [create delete index show]]
            [jiksnu.mock :as mock]
            [jiksnu.model.conversation :as model.conversation]
            [jiksnu.modules.core.factory :as f]
            [jiksnu.test-helper :as th]
            [midje.sweet :refer :all]))

(th/module-test ["jiksnu.modules.core"])

(facts "#'create"
  (let [domain (mock/a-domain-exists)
        domain-name (:_id domain)
        url (f/make-uri domain-name)
        source (mock/a-feed-source-exists {:domain domain
                                           :url url})
        params (factory :conversation {:domain domain-name
                                       :url url
                                       :local (:local domain)
                                       :update-source (:_id source)})]
    (create params)) => map?)

(facts "#'delete"
  (let [conversation (mock/a-conversation-exists)]
    (delete conversation) => map?
    (model.conversation/fetch-by-id (:_id conversation)) => nil))

(facts "#'index"
  (fact "should return a page structure"
    (index) => map?))

(facts "#'show"
  (show .conversation.) => .conversation.)
